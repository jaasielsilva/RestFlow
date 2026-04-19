package com.jaasielsilva.erpcorporativo.app.dto.web.os;

import java.util.List;

import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;

public record OrdemServicoListViewModel(
        List<OrdemServicoViewModel> items,
        int currentPage,
        int totalPages,
        long totalElements,
        // KPIs do topo
        long totalAbertas,
        long totalEmAndamento,
        long totalConcluidas,
        long totalCanceladas
) {
    public boolean hasNext() { return currentPage + 1 < totalPages; }
    public boolean hasPrev() { return currentPage > 0; }

    public String statusLabel(OrdemServicoStatus status) {
        return switch (status) {
            case ABERTA -> "Aberta";
            case EM_ANDAMENTO -> "Em andamento";
            case AGUARDANDO_CLIENTE -> "Aguardando cliente";
            case CONCLUIDA -> "Concluída";
            case CANCELADA -> "Cancelada";
        };
    }
}
