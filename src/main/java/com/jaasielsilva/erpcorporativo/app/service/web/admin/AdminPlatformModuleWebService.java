package com.jaasielsilva.erpcorporativo.app.service.web.admin;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminModuleCreateForm;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.PlatformModuleAdminUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPlatformModuleWebService {

    private final PlatformModuleAdminUseCase platformModuleAdminUseCase;

    public List<PlatformModule> listModules() {
        return platformModuleAdminUseCase.listModules();
    }

    public PlatformModule create(AdminModuleCreateForm form) {
        return platformModuleAdminUseCase.createModule(
                form.getCodigo(), form.getNome(), form.getDescricao(), form.getRota(), form.isAtivo());
    }

    public Tenant getTenant(Long tenantId) {
        return platformModuleAdminUseCase.findTenant(tenantId);
    }

    public Map<Long, Boolean> getTenantModuleStates(Long tenantId) {
        return platformModuleAdminUseCase.getTenantModuleStates(tenantId);
    }

    public PlatformModule getModule(Long id) {
        return platformModuleAdminUseCase.listModules().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException("Módulo não encontrado"));
    }

    public void update(Long id, AdminModuleCreateForm form) {
        platformModuleAdminUseCase.updateModule(id, form);
    }

    public void delete(Long id) {
        platformModuleAdminUseCase.deleteModule(id);
    }

    public void setTenantModule(Long tenantId, Long moduleId, boolean enabled) {
        platformModuleAdminUseCase.setTenantModule(tenantId, moduleId, enabled);
    }
}
