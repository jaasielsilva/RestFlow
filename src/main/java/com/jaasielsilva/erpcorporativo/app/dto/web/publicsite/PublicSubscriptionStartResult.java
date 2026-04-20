package com.jaasielsilva.erpcorporativo.app.dto.web.publicsite;

public record PublicSubscriptionStartResult(
        Long onboardingId,
        Long tenantId,
        Long paymentRecordId,
        String externalReference,
        String checkoutUrl
) {
}
