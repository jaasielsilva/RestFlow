package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import java.util.List;

public record ClienteListViewModel(
        List<ClienteResumoViewModel> items,
        int currentPage,
        int totalPages,
        long totalElements,
        long totalAtivos,
        long totalInativos,
        long totalProspectos,
        long totalBloqueados
) {
    public boolean hasNext() { return currentPage + 1 < totalPages; }
    public boolean hasPrev() { return currentPage > 0; }
}
