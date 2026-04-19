package com.jaasielsilva.erpcorporativo.app.mapper.web.home;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeRecentTenantViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;

@Component
public class HomeWebMapper {

    public HomeViewModel toViewModel(String email, List<Tenant> recentTenants) {
        List<HomeRecentTenantViewModel> recentTenantsView = recentTenants.stream()
                .map(tenant -> new HomeRecentTenantViewModel(
                        tenant.getNome(),
                        tenant.isAtivo(),
                        tenant.getCreatedAt()
                ))
                .toList();

        return new HomeViewModel(email, recentTenantsView);
    }
}
