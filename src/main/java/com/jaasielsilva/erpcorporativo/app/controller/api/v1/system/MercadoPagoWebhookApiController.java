package com.jaasielsilva.erpcorporativo.app.controller.api.v1.system;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/system/webhooks/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoWebhookApiController {

    private final MercadoPagoBillingService mercadoPagoBillingService;
    private static final Pattern PAYMENT_ID_FROM_RESOURCE = Pattern.compile(".*/v1/payments/(\\d+).*");

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestParam(name = "id", required = false) String idParam,
            @RequestParam(name = "data.id", required = false) String dataIdParam
    ) {
        String paymentId = resolvePaymentId(payload, idParam, dataIdParam);
        if (paymentId == null || paymentId.isBlank()) {
            return;
        }
        mercadoPagoBillingService.processPaymentWebhook(paymentId);
    }

    private String resolvePaymentId(Map<String, Object> payload, String idParam, String dataIdParam) {
        if (dataIdParam != null && !dataIdParam.isBlank()) {
            return dataIdParam;
        }
        if (idParam != null && !idParam.isBlank()) {
            return idParam;
        }
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> data) {
            Object dataId = data.get("id");
            if (dataId != null && !String.valueOf(dataId).isBlank()) {
                return String.valueOf(dataId);
            }
        }

        Object payloadId = payload.get("id");
        if (payloadId != null && !String.valueOf(payloadId).isBlank()) {
            return String.valueOf(payloadId);
        }

        Object resourceObj = payload.get("resource");
        if (resourceObj != null) {
            Matcher matcher = PAYMENT_ID_FROM_RESOURCE.matcher(String.valueOf(resourceObj));
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        return null;
    }
}
