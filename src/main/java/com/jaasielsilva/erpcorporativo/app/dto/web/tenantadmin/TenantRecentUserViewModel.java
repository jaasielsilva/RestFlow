package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import com.jaasielsilva.erpcorporativo.app.model.Role;

public record TenantRecentUserViewModel(
        Long id,
        String nome,
        String email,
        Role role,
        boolean ativo
) {
}
