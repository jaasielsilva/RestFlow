package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.model.PaymentProvider;
import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MercadoPagoBillingService {

    private final PlatformSettingService settingService;
    private final PaymentRecordRepository paymentRecordRepository;

    public PaymentRecord createCheckout(PaymentRecord paymentRecord, String tenantNome) {
        String token = requireAccessToken();
        String externalReference = paymentRecord.getExternalReference() != null
                ? paymentRecord.getExternalReference()
                : "pay_" + paymentRecord.getId() + "_" + UUID.randomUUID();

        Map<String, Object> payload = Map.of(
                "items", List.of(Map.of(
                        "id", String.valueOf(paymentRecord.getId()),
                        "title", "Assinatura SaaS - " + tenantNome + " - " + paymentRecord.getMesReferencia(),
                        "description", "Pagamento de assinatura mensal da plataforma SaaS",
                        "quantity", 1,
                        "currency_id", "BRL",
                        "unit_price", paymentRecord.getValorPago() != null ? paymentRecord.getValorPago() : BigDecimal.ZERO
                )),
                "external_reference", externalReference,
                "back_urls", Map.of(
                        "success", settingService.get(PlatformSettingService.MP_SUCCESS_URL, ""),
                        "failure", settingService.get(PlatformSettingService.MP_FAILURE_URL, ""),
                        "pending", settingService.get(PlatformSettingService.MP_PENDING_URL, "")
                ),
                "auto_return", "approved"
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = RestClient.create("https://api.mercadopago.com")
                .post()
                .uri("/checkout/preferences")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(payload)
                .retrieve()
                .body(Map.class);

        String checkoutUrl = response != null && response.get("init_point") != null
                ? String.valueOf(response.get("init_point"))
                : null;

        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new ValidationException("Mercado Pago não retornou URL de checkout.");
        }

        paymentRecord.setPaymentProvider(PaymentProvider.MERCADO_PAGO);
        paymentRecord.setExternalReference(externalReference);
        paymentRecord.setCheckoutUrl(checkoutUrl);
        return paymentRecordRepository.save(paymentRecord);
    }

    public void processPaymentWebhook(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return;
        }
        String token = requireAccessToken();

        @SuppressWarnings("unchecked")
        Map<String, Object> payment = RestClient.create("https://api.mercadopago.com")
                .get()
                .uri("/v1/payments/{id}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(Map.class);

        if (payment == null) {
            return;
        }

        String externalReference = payment.get("external_reference") != null
                ? String.valueOf(payment.get("external_reference"))
                : null;
        if (externalReference == null) {
            return;
        }

        PaymentRecord record = paymentRecordRepository.findByExternalReference(externalReference).orElse(null);
        if (record == null) {
            return;
        }

        String status = payment.get("status") != null ? String.valueOf(payment.get("status")) : "";
        record.setProviderPaymentId(paymentId);
        record.setStatus(mapPaymentStatus(status));
        if ("approved".equalsIgnoreCase(status)) {
            record.setDataPagamento(LocalDate.now());
        }
        paymentRecordRepository.save(record);
    }

    public String testCredentials() {
        String token = requireAccessToken();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> account = RestClient.create("https://api.mercadopago.com")
                    .get()
                    .uri("/users/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(Map.class);
            if (account == null) {
                throw new ValidationException("Mercado Pago não retornou dados da conta.");
            }
            Object nickname = account.get("nickname");
            Object email = account.get("email");
            if (nickname != null) {
                return String.valueOf(nickname);
            }
            if (email != null) {
                return String.valueOf(email);
            }
            return "conta validada";
        } catch (RestClientResponseException ex) {
            throw new ValidationException("Erro HTTP " + ex.getStatusCode().value() + " ao validar credenciais.");
        }
    }

    private PaymentStatus mapPaymentStatus(String status) {
        return switch (status.toLowerCase()) {
            case "approved" -> PaymentStatus.PAGO;
            case "cancelled", "rejected", "refunded", "charged_back" -> PaymentStatus.CANCELADO;
            case "in_process", "pending", "authorized" -> PaymentStatus.PENDENTE;
            default -> PaymentStatus.PENDENTE;
        };
    }

    private String requireAccessToken() {
        String token = settingService.get(PlatformSettingService.MP_ACCESS_TOKEN, "");
        if (token == null || token.isBlank()) {
            throw new ValidationException("Mercado Pago não configurado. Defina billing.mp.access_token.");
        }
        return token;
    }
}
