package com.jaasielsilva.erpcorporativo.app.dto.api.admin.user;

import com.jaasielsilva.erpcorporativo.app.model.Role;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        boolean ativo,
        Role role,
        Long tenantId,
        String tenantNome
) {
}
