package com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan;

import java.time.LocalDateTime;
import java.util.List;

public record SubscriptionPlanResponse(
        Long id,
        String codigo,
        String nome,
        String descricao,
        boolean ativo,
        List<PlanModuleItem> modules,
        LocalDateTime createdAt
) {
    public record PlanModuleItem(Long id, String codigo, String nome) {}
}
