package com.jaasielsilva.erpcorporativo.app.service.web.admin;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminTenantsPageViewModel;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.TenantAdminApiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTenantWebService {

    private final TenantAdminApiService tenantAdminApiService;

    public AdminTenantsPageViewModel list(String nome, String slug, Boolean ativo, int page, int size) {
        PageResponse<TenantResponse> response = tenantAdminApiService.list(new TenantFilter(nome, slug, ativo), page, size);
        return new AdminTenantsPageViewModel(response.content(), response.page(), response.totalPages());
    }
}
