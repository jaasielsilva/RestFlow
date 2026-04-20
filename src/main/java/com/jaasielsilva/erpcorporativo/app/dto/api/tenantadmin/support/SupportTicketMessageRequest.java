package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import com.jaasielsilva.erpcorporativo.app.model.SupportMessageVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupportTicketMessageRequest(
        @NotBlank(message = "Mensagem é obrigatória")
        @Size(max = 5000, message = "Mensagem deve ter no máximo 5000 caracteres")
        String conteudo,
        @NotNull(message = "Visibilidade é obrigatória")
        SupportMessageVisibility visibilidade
) {
}
