package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow;

import java.time.LocalDateTime;

public record WorkflowRuleResponse(
        Long id,
        String nome,
        String eventType,
        String conditionExpression,
        String actionType,
        String actionPayload,
        boolean ativo,
        LocalDateTime createdAt
) {
}
