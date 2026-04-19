package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.List;
import java.util.Map;

import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;

/**
 * Matriz de permissões de um tenant: módulos habilitados x roles (ADMIN, USER).
 * matrix: moduleId -> role -> accessLevel
 */
public record TenantPermissionMatrixViewModel(
        Tenant tenant,
        List<PlatformModule> enabledModules,
        Map<Long, Map<Role, AccessLevel>> matrix,
        AccessLevel[] accessLevels
) {
    public AccessLevel levelFor(Long moduleId, Role role) {
        Map<Role, AccessLevel> byRole = matrix.get(moduleId);
        if (byRole == null) return role == Role.ADMIN ? AccessLevel.FULL : AccessLevel.NONE;
        return byRole.getOrDefault(role, role == Role.ADMIN ? AccessLevel.FULL : AccessLevel.NONE);
    }
}
