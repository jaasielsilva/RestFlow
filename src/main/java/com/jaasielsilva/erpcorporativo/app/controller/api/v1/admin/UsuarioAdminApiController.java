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
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.UsuarioAdminApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UsuarioAdminApiController {

    private final UsuarioAdminApiService usuarioAdminApiService;

    @GetMapping
    public ApiResponse<PageResponse<UsuarioResponse>> list(
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "ativo", required = false) Boolean ativo,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        UsuarioFilter filter = new UsuarioFilter(nome, email, tenantId, ativo, role);
        return ApiResponse.success(usuarioAdminApiService.list(filter, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<UsuarioResponse> getById(@PathVariable("id") Long id) {
        return ApiResponse.success(usuarioAdminApiService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UsuarioResponse> create(@Valid @RequestBody UsuarioRequest request) {
        return ApiResponse.success(usuarioAdminApiService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UsuarioResponse> update(@PathVariable("id") Long id, @Valid @RequestBody UsuarioRequest request) {
        return ApiResponse.success(usuarioAdminApiService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        usuarioAdminApiService.delete(id);
    }
}
