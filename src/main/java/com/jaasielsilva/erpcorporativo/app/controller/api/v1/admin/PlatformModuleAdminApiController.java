package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.module.PlatformModuleRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.module.PlatformModuleResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.module.TenantModuleAccessResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.module.TenantModuleUpdateRequest;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.PlatformModuleAdminApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/modules")
@RequiredArgsConstructor
public class PlatformModuleAdminApiController {

    private final PlatformModuleAdminApiService platformModuleAdminApiService;

    @GetMapping
    public ApiResponse<List<PlatformModuleResponse>> listModules() {
        return ApiResponse.success(platformModuleAdminApiService.listModules());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PlatformModuleResponse> create(@Valid @RequestBody PlatformModuleRequest request) {
        return ApiResponse.success(platformModuleAdminApiService.create(request));
    }

    @GetMapping("/tenants/{tenantId}")
    public ApiResponse<List<TenantModuleAccessResponse>> listTenantModules(@PathVariable Long tenantId) {
        return ApiResponse.success(platformModuleAdminApiService.listTenantModules(tenantId));
    }

    @PutMapping("/tenants/{tenantId}/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setTenantModule(
            @PathVariable Long tenantId,
            @PathVariable Long moduleId,
            @RequestBody TenantModuleUpdateRequest request
    ) {
        platformModuleAdminApiService.setTenantModule(tenantId, moduleId, request.enabled());
    }
}
