package com.jaasielsilva.erpcorporativo.app.mapper.web.admin;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminDashboardViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.PlatformChartPointViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.PlatformMetricViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.RestaurantRowViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractAlertViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractKpiViewModel;

@Component
public class AdminDashboardWebMapper {

    public AdminDashboardViewModel toViewModel(
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
        return new AdminDashboardViewModel(
                email,
                totalUsuarios,
                totalTenants,
                metricas,
                crescimentoClientes,
                receitaMensal,
                distribuicaoPlanos,
                restaurantes,
                contractKpis,
                contractAlerts
        );
    }
}
