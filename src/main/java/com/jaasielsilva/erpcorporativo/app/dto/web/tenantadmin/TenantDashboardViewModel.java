package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

public record TenantDashboardViewModel(
        long totalUsuarios,
        long usuariosAtivos,
        long adminsAtivos,
        int modulosHabilitados
) {
}
