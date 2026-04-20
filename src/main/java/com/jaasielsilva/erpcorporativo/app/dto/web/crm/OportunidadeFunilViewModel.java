package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OportunidadeFunilViewModel(
        Map<String, List<OportunidadeViewModel>> porStatus,
        long totalAbertas,
        BigDecimal valorTotalEstimado,
        long fechadasGanhasNoMes,
        long totalFechadasNoMes,
        List<ClienteResumoViewModel> clientes
) {
    public double taxaConversao() {
        if (totalFechadasNoMes == 0) return 0.0;
        return (double) fechadasGanhasNoMes / totalFechadasNoMes * 100.0;
    }
}
