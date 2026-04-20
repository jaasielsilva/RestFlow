package com.jaasielsilva.erpcorporativo.app.controller.api.v1.system;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SuppressWarnings("unchecked")
    public void receive(@RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null) {
            return;
        }
        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            return;
        }
        Object paymentId = data.get("id");
        if (paymentId != null) {
            mercadoPagoBillingService.processPaymentWebhook(String.valueOf(paymentId));
        }
    }
}
