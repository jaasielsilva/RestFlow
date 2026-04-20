package com.jaasielsilva.erpcorporativo.app.dto.web.support;

import java.time.LocalDateTime;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

public record SupportTicketListItemViewModel(
        Long id,
        String numero,
        String assunto,
        String categoria,
        SupportTicketStatus status,
        SupportTicketPriority prioridade,
        SupportSlaState slaState,
        String clienteNome,
        String responsavelNome,
        LocalDateTime resolutionDueAt,
        LocalDateTime updatedAt
) {
}
