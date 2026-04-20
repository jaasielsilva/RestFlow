package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.permission.TenantRolePermissionRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.permission.TenantRolePermissionResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.TenantRolePermissionApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/tenants/{tenantId}/permissions")
@RequiredArgsConstructor
public class TenantRolePermissionApiController {

    private final TenantRolePermissionApiService tenantRolePermissionApiService;

    @GetMapping
    public ApiResponse<List<TenantRolePermissionResponse>> listByTenant(@PathVariable("tenantId") Long tenantId) {
        return ApiResponse.success(tenantRolePermissionApiService.listByTenant(tenantId));
    }

    @PutMapping
    public ApiResponse<TenantRolePermissionResponse> setPermission(
            @PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody TenantRolePermissionRequest request
    ) {
        return ApiResponse.success(tenantRolePermissionApiService.setPermission(tenantId, request));
    }
}
