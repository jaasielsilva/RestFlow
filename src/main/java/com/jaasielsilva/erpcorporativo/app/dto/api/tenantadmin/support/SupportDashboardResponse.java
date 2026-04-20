package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

public record SupportDashboardResponse(
        long totalBacklog,
        long totalAbertos,
        long totalResolvidosPeriodo,
        long totalViolados,
        double mediaHorasPrimeiraResposta,
        double mediaHorasResolucao,
        double percentualSlaCumprido
) {
}
