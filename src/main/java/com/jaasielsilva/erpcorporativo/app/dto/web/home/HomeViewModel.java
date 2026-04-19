package com.jaasielsilva.erpcorporativo.app.dto.web.home;

import java.util.List;

public record HomeViewModel(
        String email,
        long tenantsAtivos,
        long usuariosAtivos,
        long modulosAtivos,
        long mrrTotal,
        List<HomeRecentTenantViewModel> recentTenants,
        List<String> chartLabels,
        List<Long> chartTenants,
        List<Long> chartUsuarios,
        List<HomeRecentLogViewModel> recentLogs
) {
}
