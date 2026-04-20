package com.jaasielsilva.erpcorporativo.app.dto.web.support;

public record SupportDashboardViewModel(
        long totalBacklog,
        long totalAbertos,
        long totalResolvidosPeriodo,
        long totalViolados,
        double mediaHorasPrimeiraResposta,
        double mediaHorasResolucao,
        double percentualSlaCumprido
) {
}
