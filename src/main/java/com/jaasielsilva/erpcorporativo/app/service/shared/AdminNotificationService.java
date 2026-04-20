package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.NotificationAudience;
import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PlatformNotification;
import com.jaasielsilva.erpcorporativo.app.repository.notification.PlatformNotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final PlatformNotificationRepository platformNotificationRepository;

    @Transactional
    public void createPaymentReceivedNotifications(PaymentRecord paymentRecord) {
        String providerPaymentId = paymentRecord.getProviderPaymentId();
        if (providerPaymentId == null || providerPaymentId.isBlank()) {
            return;
        }
        createAdminPaymentNotification(paymentRecord, providerPaymentId);
        createTenantPaymentNotification(paymentRecord, providerPaymentId);
    }

    @Transactional(readOnly = true)
    public long unreadCountForAdmin() {
        return platformNotificationRepository.countByAudienceAndReadAtIsNull(NotificationAudience.ADMIN);
    }

    @Transactional(readOnly = true)
    public long unreadCountForTenant(Long tenantId) {
        return platformNotificationRepository.countByAudienceAndTenantIdAndReadAtIsNull(NotificationAudience.TENANT, tenantId);
    }

    @Transactional(readOnly = true)
    public List<PlatformNotification> latestForAdmin() {
        return platformNotificationRepository.findTop10ByAudienceOrderByCreatedAtDesc(NotificationAudience.ADMIN);
    }

    @Transactional(readOnly = true)
    public List<PlatformNotification> latestForTenant(Long tenantId) {
        return platformNotificationRepository.findTop10ByAudienceAndTenantIdOrderByCreatedAtDesc(NotificationAudience.TENANT, tenantId);
    }

    @Transactional(readOnly = true)
    public List<PlatformNotification> filterForAdmin(String type, String readStatus, LocalDate dateFrom, LocalDate dateTo) {
        var base = platformNotificationRepository.findTop200ByAudienceOrderByCreatedAtDesc(NotificationAudience.ADMIN);
        return filter(base, type, readStatus, dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public List<PlatformNotification> filterForTenant(Long tenantId, String type, String readStatus, LocalDate dateFrom, LocalDate dateTo) {
        var base = platformNotificationRepository.findTop200ByAudienceAndTenantIdOrderByCreatedAtDesc(NotificationAudience.TENANT, tenantId);
        return filter(base, type, readStatus, dateFrom, dateTo);
    }

    @Transactional
    public int markAllAsReadForAdmin() {
        return platformNotificationRepository.markAllAsRead(NotificationAudience.ADMIN, LocalDateTime.now());
    }

    @Transactional
    public int markAllAsReadForTenant(Long tenantId) {
        return platformNotificationRepository.markAllAsReadByTenant(NotificationAudience.TENANT, tenantId, LocalDateTime.now());
    }

    private boolean createAdminPaymentNotification(PaymentRecord paymentRecord, String providerPaymentId) {
        String sourceKey = "admin:payment_paid:" + providerPaymentId;
        if (platformNotificationRepository.existsBySourceKey(sourceKey)) {
            return false;
        }

        String tenantName = paymentRecord.getContract() != null && paymentRecord.getContract().getTenant() != null
                ? paymentRecord.getContract().getTenant().getNome()
                : "Tenant";
        String amount = paymentRecord.getValorPago() != null
                ? NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(paymentRecord.getValorPago())
                : "R$ 0,00";
        String reference = paymentRecord.getMesReferencia() != null ? paymentRecord.getMesReferencia().toString() : "-";

        PlatformNotification notification = PlatformNotification.builder()
                .sourceKey(sourceKey)
                .title("Pagamento confirmado no Mercado Pago")
                .message("Pagamento " + amount + " (" + reference + ") confirmado para " + tenantName + ".")
                .audience(NotificationAudience.ADMIN)
                .type("PAYMENT")
                .build();
        platformNotificationRepository.save(notification);
        return true;
    }

    private boolean createTenantPaymentNotification(PaymentRecord paymentRecord, String providerPaymentId) {
        Long tenantId = paymentRecord.getContract() != null && paymentRecord.getContract().getTenant() != null
                ? paymentRecord.getContract().getTenant().getId()
                : null;
        if (tenantId == null) {
            return false;
        }

        String sourceKey = "tenant:" + tenantId + ":payment_paid:" + providerPaymentId;
        if (platformNotificationRepository.existsBySourceKey(sourceKey)) {
            return false;
        }

        String amount = paymentRecord.getValorPago() != null
                ? NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(paymentRecord.getValorPago())
                : "R$ 0,00";
        String reference = paymentRecord.getMesReferencia() != null ? paymentRecord.getMesReferencia().toString() : "-";

        PlatformNotification notification = PlatformNotification.builder()
                .sourceKey(sourceKey)
                .title("Pagamento aprovado")
                .message("Recebemos seu pagamento de " + amount + " referente a " + reference + ".")
                .audience(NotificationAudience.TENANT)
                .tenantId(tenantId)
                .type("PAYMENT")
                .build();
        platformNotificationRepository.save(notification);
        return true;
    }

    private List<PlatformNotification> filter(
            List<PlatformNotification> base,
            String type,
            String readStatus,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        Stream<PlatformNotification> stream = base.stream();
        if (type != null && !type.isBlank()) {
            String normalized = type.trim().toUpperCase(Locale.ROOT);
            stream = stream.filter(n -> normalized.equals(n.getType()));
        }
        if ("READ".equalsIgnoreCase(readStatus)) {
            stream = stream.filter(n -> n.getReadAt() != null);
        } else if ("UNREAD".equalsIgnoreCase(readStatus)) {
            stream = stream.filter(n -> n.getReadAt() == null);
        }
        if (dateFrom != null) {
            stream = stream.filter(n -> n.getCreatedAt() != null && !n.getCreatedAt().toLocalDate().isBefore(dateFrom));
        }
        if (dateTo != null) {
            stream = stream.filter(n -> n.getCreatedAt() != null && !n.getCreatedAt().toLocalDate().isAfter(dateTo));
        }
        return stream.toList();
    }
}
