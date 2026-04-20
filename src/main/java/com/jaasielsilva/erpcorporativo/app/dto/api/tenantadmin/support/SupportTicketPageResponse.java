package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;

public record SupportTicketPageResponse(
        PageResponse<SupportTicketListItemResponse> page,
        long totalAbertos,
        long totalAtendimento,
        long totalResolvidos,
        long totalViolados
) {
}
