package com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan;

import jakarta.validation.constraints.NotNull;

public record AssignPlanRequest(
        @NotNull(message = "planId é obrigatório")
        Long planId
) {
}
