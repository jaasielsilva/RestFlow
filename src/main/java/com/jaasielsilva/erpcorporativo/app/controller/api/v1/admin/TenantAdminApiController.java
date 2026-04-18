package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant.TenantResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.TenantAdminApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@RequiredArgsConstructor
public class TenantAdminApiController {

    private final TenantAdminApiService tenantAdminApiService;

    @GetMapping
    public ApiResponse<PageResponse<TenantResponse>> list(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TenantFilter filter = new TenantFilter(nome, slug, ativo);
        return ApiResponse.success(tenantAdminApiService.list(filter, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<TenantResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(tenantAdminApiService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TenantResponse> create(@Valid @RequestBody TenantRequest request) {
        return ApiResponse.success(tenantAdminApiService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TenantResponse> update(@PathVariable Long id, @Valid @RequestBody TenantRequest request) {
        return ApiResponse.success(tenantAdminApiService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tenantAdminApiService.delete(id);
    }
}
