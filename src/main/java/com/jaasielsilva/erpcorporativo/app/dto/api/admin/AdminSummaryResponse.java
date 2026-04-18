package com.jaasielsilva.erpcorporativo.app.dto.api.admin;

public record AdminSummaryResponse(
        long totalUsuarios,
        long totalTenants
) {
}
