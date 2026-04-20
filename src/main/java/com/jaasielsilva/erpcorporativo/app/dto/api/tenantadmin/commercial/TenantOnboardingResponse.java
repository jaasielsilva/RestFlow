package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial;

import com.jaasielsilva.erpcorporativo.app.model.OnboardingStatus;

public record TenantOnboardingResponse(
        OnboardingStatus status,
        String templateCode,
        boolean companyProfileCompleted,
        boolean teamInvited,
        boolean modulesConfigured,
        boolean firstTicketCreated,
        boolean firstOrderCreated,
        int completionPercent
) {
}
