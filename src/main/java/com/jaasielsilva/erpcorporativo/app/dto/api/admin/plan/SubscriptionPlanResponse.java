package com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan;

import java.time.LocalDateTime;
import java.util.List;

import com.jaasielsilva.erpcorporativo.app.model.PlanTier;

public record SubscriptionPlanResponse(
        Long id,
        String codigo,
        String nome,
        String descricao,
        boolean ativo,
        PlanTier tier,
        Integer maxUsers,
        Integer maxStorageGb,
        boolean annualDiscountEligible,
        String onboardingTemplate,
        List<PlanModuleItem> modules,
        List<PlanAddonItem> addons,
        LocalDateTime createdAt
) {
    public record PlanModuleItem(Long id, String codigo, String nome) {}
    public record PlanAddonItem(Long id, String codigo, String nome) {}
}
