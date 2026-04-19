package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantUserForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantUsersPageViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantAdminUserApiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantUserWebService {

    private static final String DEFAULT_RESET_PASSWORD = "mudar123";

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

    public UsuarioResponse getById(Authentication authentication, Long id) {
        return tenantAdminUserApiService.getById(authentication, id);
    }

    public UsuarioResponse create(Authentication authentication, TenantUserForm form) {
        return tenantAdminUserApiService.create(authentication, toRequest(form));
    }

    public UsuarioResponse update(Authentication authentication, Long id, TenantUserForm form) {
        return tenantAdminUserApiService.update(authentication, id, toRequest(form));
    }

    public UsuarioResponse toggleActive(Authentication authentication, Long id) {
        UsuarioResponse current = tenantAdminUserApiService.getById(authentication, id);
        UsuarioRequest request = new UsuarioRequest(
                current.nome(),
                current.email(),
                null,
                !current.ativo(),
                current.role(),
                current.tenantId()
        );
        return tenantAdminUserApiService.update(authentication, id, request);
    }

    public UsuarioResponse resetPassword(Authentication authentication, Long id) {
        UsuarioResponse current = tenantAdminUserApiService.getById(authentication, id);
        UsuarioRequest request = new UsuarioRequest(
                current.nome(),
                current.email(),
                DEFAULT_RESET_PASSWORD,
                current.ativo(),
                current.role(),
                current.tenantId()
        );
        return tenantAdminUserApiService.update(authentication, id, request);
    }

    private UsuarioRequest toRequest(TenantUserForm form) {
        return new UsuarioRequest(
                form.getNome(),
                form.getEmail(),
                form.getPassword(),
                form.isAtivo(),
                form.getRole(),
                null
        );
    }
}
