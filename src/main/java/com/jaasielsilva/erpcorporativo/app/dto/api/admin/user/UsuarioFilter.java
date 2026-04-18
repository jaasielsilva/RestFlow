package com.jaasielsilva.erpcorporativo.app.dto.api.admin.user;

import com.jaasielsilva.erpcorporativo.app.model.Role;

public record UsuarioFilter(
        String nome,
        String email,
        Long tenantId,
        Boolean ativo,
        Role role
) {
}
