package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import java.time.LocalDateTime;
import java.util.List;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

public record SupportTicketResponse(
        Long id,
        String numero,
        String assunto,
        String descricao,
        String categoria,
        SupportTicketStatus status,
        SupportTicketPriority prioridade,
        SupportSlaState slaState,
        Long clienteId,
        String clienteNome,
        String solicitanteNome,
        String solicitanteEmail,
        Long responsavelId,
        String responsavelNome,
        LocalDateTime firstResponseDueAt,
        LocalDateTime resolutionDueAt,
        LocalDateTime firstRespondedAt,
        LocalDateTime resolvedAt,
        LocalDateTime closedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SupportMessageResponse> mensagens,
        List<SupportAttachmentResponse> anexos
) {
}
