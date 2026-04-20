package com.jaasielsilva.erpcorporativo.app.dto.api.admin;

import java.time.LocalDateTime;

import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscription;

public record OnboardingSubscriptionSummaryResponse(
        Long onboardingId,
        String tenantNome,
        String adminEmail,
        String status,
        String externalReference,
        String checkoutUrl,
        Long paymentRecordId,
        LocalDateTime createdAt,
        LocalDateTime activatedAt
) {
    public static OnboardingSubscriptionSummaryResponse from(OnboardingSubscription subscription) {
        return new OnboardingSubscriptionSummaryResponse(
                subscription.getId(),
                subscription.getTenantNome(),
                subscription.getAdminEmail(),
                subscription.getStatus().name(),
                subscription.getExternalReference(),
                subscription.getCheckoutUrl(),
                subscription.getPaymentRecordId(),
                subscription.getCreatedAt(),
                subscription.getActivatedAt()
        );
    }
}
