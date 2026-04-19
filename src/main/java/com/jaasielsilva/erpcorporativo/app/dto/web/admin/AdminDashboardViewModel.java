package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.List;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractAlertViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractKpiViewModel;

public record AdminDashboardViewModel(
        String email,
        long totalUsuarios,
        long totalTenants,
        List<PlatformMetricViewModel> metricas,
        List<PlatformChartPointViewModel> crescimentoClientes,
        List<PlatformChartPointViewModel> receitaMensal,
        List<PlatformChartPointViewModel> distribuicaoPlanos,
        List<RestaurantRowViewModel> restaurantes,
        ContractKpiViewModel contractKpis,
        List<ContractAlertViewModel> contractAlerts
) {
}
