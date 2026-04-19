package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import java.math.BigDecimal;
import java.util.List;

public record TenantDashboardViewModel(
        // Equipe
        long totalUsuarios,
        long usuariosAtivos,
        long adminsAtivos,
        int modulosHabilitados,

        // OS — visível para todos (READ+)
        long osAbertas,
        long osEmAndamento,
        long osConcluidas,
        long osCanceladas,
        List<OsRecenteViewModel> osRecentes,

        // Financeiro — ADMIN only
        BigDecimal faturamentoMes,
        BigDecimal faturamentoTotal,
        long osPendentesValor,

        // Equipe — ADMIN only
        List<ResponsavelCargaViewModel> cargaPorResponsavel,

        // Chart labels/data — ADMIN only
        List<String> chartLabels,
        List<Long> chartAbertas,
        List<Long> chartConcluidas
) {
    public record OsRecenteViewModel(
            Long id,
            String numero,
            String titulo,
            String clienteNome,
            String status,
            String statusCss
    ) {}

    public record ResponsavelCargaViewModel(
            String nome,
            long total,
            long abertas,
            long concluidas
    ) {}
}
