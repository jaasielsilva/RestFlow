package com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.UsuarioAdminUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantAdminUserApiService {

    private final UsuarioAdminUseCase usuarioAdminUseCase;

    public PageResponse<UsuarioResponse> list(Authentication authentication, UsuarioFilter filter, int page, int size) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        return usuarioAdminUseCase.listByTenant(currentUser.getTenantId(), filter, page, size);
    }

    public UsuarioResponse getById(Authentication authentication, Long id) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        return usuarioAdminUseCase.getByIdForTenant(currentUser.getTenantId(), id);
    }

    public UsuarioResponse create(Authentication authentication, UsuarioRequest request) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        return usuarioAdminUseCase.createForTenantAdmin(currentUser.getTenantId(), request);
    }

    public UsuarioResponse update(Authentication authentication, Long id, UsuarioRequest request) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        return usuarioAdminUseCase.updateForTenantAdmin(currentUser.getTenantId(), id, request);
    }

    public void delete(Authentication authentication, Long id) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        usuarioAdminUseCase.deleteForTenantAdmin(currentUser.getTenantId(), id);
    }
}
