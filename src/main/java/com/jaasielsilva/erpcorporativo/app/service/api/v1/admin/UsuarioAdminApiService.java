package com.jaasielsilva.erpcorporativo.app.service.api.v1.admin;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.UsuarioAdminUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioAdminApiService {

    private final UsuarioAdminUseCase usuarioAdminUseCase;

    public PageResponse<UsuarioResponse> list(UsuarioFilter filter, int page, int size) {
        return usuarioAdminUseCase.list(filter, page, size);
    }

    public UsuarioResponse getById(Long id) {
        return usuarioAdminUseCase.getById(id);
    }

    public UsuarioResponse create(UsuarioRequest request) {
        return usuarioAdminUseCase.create(request);
    }

    public UsuarioResponse update(Long id, UsuarioRequest request) {
        return usuarioAdminUseCase.update(id, request);
    }

    public void delete(Long id) {
        usuarioAdminUseCase.delete(id);
    }
}
