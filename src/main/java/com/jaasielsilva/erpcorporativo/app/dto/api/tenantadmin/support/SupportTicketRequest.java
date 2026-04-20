package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupportTicketRequest(
        @NotBlank(message = "Assunto é obrigatório")
        @Size(max = 200, message = "Assunto deve ter no máximo 200 caracteres")
        String assunto,
        @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
        String descricao,
        @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
        String categoria,
        @NotNull(message = "Status é obrigatório")
        SupportTicketStatus status,
        @NotNull(message = "Prioridade é obrigatória")
        SupportTicketPriority prioridade,
        Long clienteId,
        Long responsavelId,
        @Size(max = 150, message = "Nome do solicitante deve ter no máximo 150 caracteres")
        String solicitanteNome,
        @Email(message = "Email do solicitante inválido")
        @Size(max = 150, message = "Email do solicitante deve ter no máximo 150 caracteres")
        String solicitanteEmail
) {
}
