package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import java.util.List;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;

public record TenantUsersPageViewModel(
        List<UsuarioResponse> usuarios,
        int page,
        int totalPages
) {
}
