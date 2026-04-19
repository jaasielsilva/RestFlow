package com.jaasielsilva.erpcorporativo.app.service.api.v1.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.permission.TenantRolePermissionRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.permission.TenantRolePermissionResponse;
import com.jaasielsilva.erpcorporativo.app.model.TenantRolePermission;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.TenantRolePermissionUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantRolePermissionApiService {

    private final TenantRolePermissionUseCase tenantRolePermissionUseCase;

    public List<TenantRolePermissionResponse> listByTenant(Long tenantId) {
        return tenantRolePermissionUseCase.listByTenant(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TenantRolePermissionResponse setPermission(Long tenantId, TenantRolePermissionRequest request) {
        TenantRolePermission saved = tenantRolePermissionUseCase.setPermission(
                tenantId,
                request.moduleId(),
                request.role(),
                request.accessLevel()
        );
        return toResponse(saved);
    }

    private TenantRolePermissionResponse toResponse(TenantRolePermission p) {
        return new TenantRolePermissionResponse(
                p.getId(),
                p.getTenant().getId(),
                p.getTenant().getNome(),
                p.getModule().getId(),
                p.getModule().getCodigo(),
                p.getModule().getNome(),
                p.getRole(),
                p.getAccessLevel()
        );
    }
}
