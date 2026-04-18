package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.List;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantResponse;

public record AdminTenantsPageViewModel(
        List<TenantResponse> tenants,
        int page,
        int totalPages
) {
}
