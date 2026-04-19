package com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract;

import java.math.BigDecimal;
import java.util.List;

public record ContractListViewModel(
        List<ContractViewModel> items,
        int currentPage,
        int totalPages,
        long totalElements,
        long totalAtivos,
        long totalVencidos,
        long totalAtrasados,
        BigDecimal mrrTotal
) {
    public boolean hasNext() { return currentPage + 1 < totalPages; }
    public boolean hasPrev() { return currentPage > 0; }
}
