package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.List;

public record AdminTenantsPageViewModel(
        List<AdminTenantListItemViewModel> tenants,
        int page,
        int totalPages
) {
}
