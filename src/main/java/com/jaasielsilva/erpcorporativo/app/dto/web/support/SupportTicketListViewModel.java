package com.jaasielsilva.erpcorporativo.app.dto.web.support;

import java.util.List;

public record SupportTicketListViewModel(
        List<SupportTicketListItemViewModel> items,
        int currentPage,
        int totalPages,
        long totalElements,
        long totalAbertos,
        long totalAtendimento,
        long totalResolvidos,
        long totalViolados
) {
    public boolean hasNext() {
        return currentPage + 1 < totalPages;
    }

    public boolean hasPrev() {
        return currentPage > 0;
    }
}
