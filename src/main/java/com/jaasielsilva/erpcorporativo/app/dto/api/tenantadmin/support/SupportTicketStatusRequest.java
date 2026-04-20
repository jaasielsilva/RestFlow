package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

import jakarta.validation.constraints.NotNull;

public record SupportTicketStatusRequest(
        @NotNull(message = "Status é obrigatório")
        SupportTicketStatus status
) {
}
