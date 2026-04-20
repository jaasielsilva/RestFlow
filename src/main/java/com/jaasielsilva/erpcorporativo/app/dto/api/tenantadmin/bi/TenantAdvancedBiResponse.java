package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.bi;

import java.math.BigDecimal;

public record TenantAdvancedBiResponse(
        long totalClientes,
        long totalOportunidadesAbertas,
        long totalOrdensAbertas,
        long totalChamadosAbertos,
        long totalChamadosVioladosSla,
        BigDecimal pipelineEstimado,
        BigDecimal receitaMensalAtual,
        BigDecimal taxaConversaoOportunidades
) {
}
