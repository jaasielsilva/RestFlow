package com.jaasielsilva.erpcorporativo.app.dto.api.admin;

import com.jaasielsilva.erpcorporativo.app.service.shared.PublicOnboardingActivationService.ActivationResult;

public record OnboardingActivationResponse(
        Long onboardingId,
        Long tenantId,
        String adminEmail,
        boolean activated,
        boolean alreadyActive,
        boolean emailSent,
        String message
) {
    public static OnboardingActivationResponse from(ActivationResult result) {
        return new OnboardingActivationResponse(
                result.onboardingId(),
                result.tenantId(),
                result.adminEmail(),
                result.activated(),
                result.alreadyActive(),
                result.emailSent(),
                result.message()
        );
    }
}
