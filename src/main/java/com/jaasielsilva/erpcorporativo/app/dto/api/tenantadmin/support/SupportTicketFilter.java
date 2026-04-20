package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

public record SupportTicketFilter(
        String assunto,
        String categoria,
        SupportTicketStatus status,
        SupportTicketPriority prioridade,
        SupportSlaState slaState,
        Long clienteId,
        Long responsavelId
) {
}
