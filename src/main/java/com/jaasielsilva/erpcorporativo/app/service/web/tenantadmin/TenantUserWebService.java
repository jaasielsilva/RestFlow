package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantUsersPageViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantAdminUserApiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantUserWebService {

    private final TenantAdminUserApiService tenantAdminUserApiService;

    public TenantUsersPageViewModel list(
            Authentication authentication,
            String nome,
            String email,
            Boolean ativo,
            Role role,
            int page,
            int size
    ) {
        PageResponse<UsuarioResponse> response = tenantAdminUserApiService.list(
                authentication,
                new UsuarioFilter(nome, email, null, ativo, role),
                page,
                size
        );

        return new TenantUsersPageViewModel(response.content(), response.page(), response.totalPages());
    }
}
