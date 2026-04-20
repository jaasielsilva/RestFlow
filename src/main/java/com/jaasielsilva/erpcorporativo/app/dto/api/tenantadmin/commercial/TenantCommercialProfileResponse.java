package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial;

import java.util.List;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanResponse.PlanAddonItem;

public record TenantCommercialProfileResponse(
        Long tenantId,
        String tenantNome,
        String planCodigo,
        String planNome,
        String planTier,
        Integer maxUsers,
        Integer maxStorageGb,
        boolean annualDiscountEligible,
        List<PlanAddonItem> addons,
        TenantBillingProfileResponse billingProfile,
        TenantOnboardingResponse onboarding
) {
}
