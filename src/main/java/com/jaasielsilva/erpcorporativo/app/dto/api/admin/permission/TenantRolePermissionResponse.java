package com.jaasielsilva.erpcorporativo.app.dto.api.admin.permission;

import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.Role;

public record TenantRolePermissionResponse(
        Long id,
        Long tenantId,
        String tenantNome,
        Long moduleId,
        String moduleCodigo,
        String moduleNome,
        Role role,
        AccessLevel accessLevel
) {
}
