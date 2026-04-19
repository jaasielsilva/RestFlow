package com.jaasielsilva.erpcorporativo.app.dto.api.admin.permission;

import jakarta.validation.constraints.NotNull;

import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.Role;

public record TenantRolePermissionRequest(
        @NotNull(message = "moduleId é obrigatório")
        Long moduleId,

        @NotNull(message = "role é obrigatória")
        Role role,

        @NotNull(message = "accessLevel é obrigatório")
        AccessLevel accessLevel
) {
}
