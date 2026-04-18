package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.List;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;

public record AdminUsersPageViewModel(
        List<UsuarioResponse> usuarios,
        int page,
        int totalPages
) {
}
