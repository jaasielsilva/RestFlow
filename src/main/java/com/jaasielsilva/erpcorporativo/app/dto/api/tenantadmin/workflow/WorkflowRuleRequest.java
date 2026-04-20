package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowRuleRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,
        @NotBlank(message = "Evento é obrigatório")
        @Size(max = 80, message = "Evento deve ter no máximo 80 caracteres")
        String eventType,
        @Size(max = 255, message = "Condição deve ter no máximo 255 caracteres")
        String conditionExpression,
        @NotBlank(message = "Ação é obrigatória")
        @Size(max = 80, message = "Ação deve ter no máximo 80 caracteres")
        String actionType,
        String actionPayload,
        boolean ativo
) {
}
