package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoBillingService.class);

    private final PlatformSettingService settingService;
    private final PaymentRecordRepository paymentRecordRepository;
    private static final Pattern MP_MESSAGE_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");

    public PaymentRecord createCheckout(PaymentRecord paymentRecord, String tenantNome) {
        String token = requireAccessToken();
        String externalReference = paymentRecord.getExternalReference() != null
                ? paymentRecord.getExternalReference()
                : "pay_" + paymentRecord.getId() + "_" + UUID.randomUUID();
        String successUrl = requireCheckoutUrl(PlatformSettingService.MP_SUCCESS_URL, "sucesso");
        String failureUrl = requireCheckoutUrl(PlatformSettingService.MP_FAILURE_URL, "falha");
        String pendingUrl = requireCheckoutUrl(PlatformSettingService.MP_PENDING_URL, "pendente");

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
                        "success", successUrl,
                        "failure", failureUrl,
                        "pending", pendingUrl
                ),
                "auto_return", "approved"
        );

        Map<String, Object> response;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rawResponse = RestClient.create("https://api.mercadopago.com")
                    .post()
                    .uri("/checkout/preferences")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            response = rawResponse;
        } catch (RestClientResponseException ex) {
            throw new ValidationException(buildMercadoPagoErrorMessage(ex));
        }

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
        reconcilePaymentByProviderId(paymentId);
    }

    public PaymentSyncResult reconcilePaymentByProviderId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return PaymentSyncResult.ignored(paymentId, "paymentId ausente.");
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
            return PaymentSyncResult.ignored(paymentId, "Mercado Pago não retornou payload do pagamento.");
        }

        String externalReference = payment.get("external_reference") != null
                ? String.valueOf(payment.get("external_reference"))
                : null;
        if (externalReference == null) {
            log.warn("[MercadoPago] pagamento {} sem external_reference, ignorando sincronização.", paymentId);
            return PaymentSyncResult.ignored(paymentId, "Pagamento sem external_reference.");
        }

        PaymentRecord record = paymentRecordRepository.findByExternalReference(externalReference).orElse(null);
        if (record == null) {
            log.warn("[MercadoPago] external_reference '{}' não encontrada localmente.", externalReference);
            return PaymentSyncResult.ignored(paymentId, "Nenhuma fatura local encontrada para external_reference.");
        }

        String status = payment.get("status") != null ? String.valueOf(payment.get("status")) : "";
        PaymentStatus statusAnterior = record.getStatus();
        String providerPaymentIdAnterior = record.getProviderPaymentId();
        record.setProviderPaymentId(paymentId);
        record.setStatus(mapPaymentStatus(status));
        if ("approved".equalsIgnoreCase(status)) {
            record.setDataPagamento(LocalDate.now());
        }
        PaymentRecord updated = paymentRecordRepository.save(record);
        boolean changed = statusAnterior != updated.getStatus()
                || providerPaymentIdAnterior == null
                || !providerPaymentIdAnterior.equals(updated.getProviderPaymentId());

        log.info(
                "[MercadoPago] sincronização concluída paymentId={} externalReference={} recordId={} statusMp={} statusAnterior={} statusAtual={}",
                paymentId, externalReference, updated.getId(), status, statusAnterior, updated.getStatus()
        );

        return PaymentSyncResult.synced(
                paymentId,
                externalReference,
                updated.getId(),
                status,
                statusAnterior,
                updated.getStatus(),
                changed
        );
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

    private String requireCheckoutUrl(String settingKey, String description) {
        String value = settingService.get(settingKey, "");
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    "Mercado Pago não configurado. Defina a URL de retorno de " + description + " nas configurações.");
        }
        return value;
    }

    private String buildMercadoPagoErrorMessage(RestClientResponseException ex) {
        String message = extractMercadoPagoMessage(ex.getResponseBodyAsString());
        if (message != null && !message.isBlank()) {
            return "Falha ao gerar checkout no Mercado Pago: " + message;
        }
        return "Falha ao gerar checkout no Mercado Pago (HTTP " + ex.getStatusCode().value() + ").";
    }

    private String extractMercadoPagoMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        Matcher matcher = MP_MESSAGE_PATTERN.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public record PaymentSyncResult(
            String paymentId,
            String externalReference,
            Long paymentRecordId,
            String mercadoPagoStatus,
            PaymentStatus localStatusBefore,
            PaymentStatus localStatusAfter,
            boolean changed,
            boolean synced,
            String message
    ) {
        public static PaymentSyncResult ignored(String paymentId, String message) {
            return new PaymentSyncResult(paymentId, null, null, null, null, null, false, false, message);
        }

        public static PaymentSyncResult synced(
                String paymentId,
                String externalReference,
                Long paymentRecordId,
                String mercadoPagoStatus,
                PaymentStatus localStatusBefore,
                PaymentStatus localStatusAfter,
                boolean changed
        ) {
            return new PaymentSyncResult(
                    paymentId,
                    externalReference,
                    paymentRecordId,
                    mercadoPagoStatus,
                    localStatusBefore,
                    localStatusAfter,
                    changed,
                    true,
                    "Sincronização concluída."
            );
        }
    }
}
