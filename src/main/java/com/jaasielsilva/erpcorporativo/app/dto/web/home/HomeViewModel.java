package com.jaasielsilva.erpcorporativo.app.dto.web.home;

import java.util.List;

public record HomeViewModel(
        String email,
        List<HomeRecentTenantViewModel> recentTenants
) {
}
