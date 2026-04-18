package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.List;

public record AdminDashboardViewModel(
        String email,
        long totalUsuarios,
        long totalTenants,
        List<PlatformMetricViewModel> metricas,
        List<PlatformChartPointViewModel> crescimentoClientes,
        List<PlatformChartPointViewModel> receitaMensal,
        List<PlatformChartPointViewModel> distribuicaoPlanos,
        List<RestaurantRowViewModel> restaurantes
) {
}
