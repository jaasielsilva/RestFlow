package com.jaasielsilva.erpcorporativo.app.service.web.admin;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminUsersPageViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.UsuarioAdminApiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserWebService {

    private final UsuarioAdminApiService usuarioAdminApiService;

    public AdminUsersPageViewModel list(
            String nome,
            String email,
            Long tenantId,
            Boolean ativo,
            Role role,
            int page,
            int size
    ) {
        PageResponse<UsuarioResponse> response = usuarioAdminApiService.list(
                new UsuarioFilter(nome, email, tenantId, ativo, role),
                page,
                size
        );
        return new AdminUsersPageViewModel(response.content(), response.page(), response.totalPages());
    }
}
