package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminSummaryResponse;
import com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin.AdminSummaryApiMapper;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetAdminSummaryUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final AdminSummaryApiMapper adminSummaryApiMapper;

    public AdminSummaryResponse execute() {
        return adminSummaryApiMapper.toResponse(
                usuarioRepository.count(),
                tenantRepository.count()
        );
    }
}
