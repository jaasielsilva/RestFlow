package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantAdminUserApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-admin/users")
@RequiredArgsConstructor
public class TenantAdminUserApiController {

    private final TenantAdminUserApiService tenantAdminUserApiService;

    @GetMapping
    public ApiResponse<PageResponse<UsuarioResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UsuarioFilter filter = new UsuarioFilter(nome, email, null, ativo, role);
        return ApiResponse.success(tenantAdminUserApiService.list(authentication, filter, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<UsuarioResponse> getById(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.success(tenantAdminUserApiService.getById(authentication, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UsuarioResponse> create(Authentication authentication, @Valid @RequestBody UsuarioRequest request) {
        return ApiResponse.success(tenantAdminUserApiService.create(authentication, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UsuarioResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request
    ) {
        return ApiResponse.success(tenantAdminUserApiService.update(authentication, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        tenantAdminUserApiService.delete(authentication, id);
    }
}
