package com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan;

import java.util.Set;

import com.jaasielsilva.erpcorporativo.app.model.PlanTier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubscriptionPlanRequest(
        @NotBlank(message = "Código é obrigatório")
        @Size(max = 60, message = "Código deve ter no máximo 60 caracteres")
        String codigo,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricao,

        boolean ativo,

        @NotNull(message = "Tier é obrigatório")
        PlanTier tier,

        Integer maxUsers,

        Integer maxStorageGb,

        boolean annualDiscountEligible,

        @Size(max = 30, message = "Template de onboarding deve ter no máximo 30 caracteres")
        String onboardingTemplate,

        Set<Long> moduleIds,

        Set<Long> addonIds
) {
}
