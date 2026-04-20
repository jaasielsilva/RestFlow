package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow;

import java.time.LocalDateTime;

public record WorkflowExecutionLogResponse(
        Long id,
        Long ruleId,
        String ruleNome,
        String eventType,
        String entityType,
        Long entityId,
        boolean success,
        String message,
        LocalDateTime createdAt
) {
}
