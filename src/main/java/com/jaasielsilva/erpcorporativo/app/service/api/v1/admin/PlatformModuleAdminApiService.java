package com.jaasielsilva.erpcorporativo.app.service.api.v1.admin;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.module.PlatformModuleRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.module.PlatformModuleResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.module.TenantModuleAccessResponse;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.PlatformModuleAdminUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlatformModuleAdminApiService {

    private final PlatformModuleAdminUseCase platformModuleAdminUseCase;

    public List<PlatformModuleResponse> listModules() {
        return platformModuleAdminUseCase.listModules().stream()
                .map(this::toResponse)
                .toList();
    }

    public PlatformModuleResponse create(PlatformModuleRequest request) {
        PlatformModule created = platformModuleAdminUseCase.createModule(
                request.codigo(),
                request.nome(),
                request.descricao(),
                request.ativo()
        );
        return toResponse(created);
    }

    public List<TenantModuleAccessResponse> listTenantModules(Long tenantId) {
        Tenant tenant = platformModuleAdminUseCase.findTenant(tenantId);
        Map<Long, Boolean> states = platformModuleAdminUseCase.getTenantModuleStates(tenantId);

        return platformModuleAdminUseCase.listModules().stream()
                .map(module -> new TenantModuleAccessResponse(
                        tenant.getId(),
                        tenant.getNome(),
                        module.getId(),
                        module.getCodigo(),
                        module.getNome(),
                        module.getDescricao(),
                        module.isAtivo(),
                        states.getOrDefault(module.getId(), false)
                ))
                .toList();
    }

    public void setTenantModule(Long tenantId, Long moduleId, boolean enabled) {
        platformModuleAdminUseCase.setTenantModule(tenantId, moduleId, enabled);
    }

    private PlatformModuleResponse toResponse(PlatformModule module) {
        return new PlatformModuleResponse(
                module.getId(),
                module.getCodigo(),
                module.getNome(),
                module.getDescricao(),
                module.isAtivo(),
                module.getCreatedAt()
        );
    }
}
