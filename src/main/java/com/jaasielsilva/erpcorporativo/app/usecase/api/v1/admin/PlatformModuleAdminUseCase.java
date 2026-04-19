package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.TenantModule;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.module.TenantModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformModuleAdminUseCase {

    private final PlatformModuleRepository platformModuleRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public List<PlatformModule> listModules() {
        return platformModuleRepository.findAll().stream()
                .sorted(Comparator.comparing(PlatformModule::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public PlatformModule createModule(String codigo, String nome, String descricao, String rota, boolean ativo) {
        validateCodigo(codigo);

        PlatformModule module = PlatformModule.builder()
                .codigo(codigo.trim())
                .nome(nome.trim())
                .descricao(descricao != null && !descricao.isBlank() ? descricao.trim() : null)
                .rota(rota != null && !rota.isBlank() ? rota.trim() : null)
                .ativo(ativo)
                .build();

        return platformModuleRepository.save(module);
    }

    @Transactional(readOnly = true)
    public Tenant findTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> getTenantModuleStates(Long tenantId) {
        return tenantModuleRepository.findAllByTenantId(tenantId).stream()
                .collect(Collectors.toMap(tm -> tm.getModule().getId(), TenantModule::isAtivo, (a, b) -> a));
    }

    @Transactional
    public void setTenantModule(Long tenantId, Long moduleId, boolean enabled) {
        Tenant tenant = findTenant(tenantId);
        PlatformModule module = platformModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado."));

        TenantModule tenantModule = tenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId)
                .orElseGet(() -> TenantModule.builder()
                        .tenant(tenant)
                        .module(module)
                        .ativo(false)
                        .build());

        tenantModule.setAtivo(enabled);
        tenantModuleRepository.save(tenantModule);
    }

    private void validateCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ConflictException("Código do módulo é obrigatório.");
        }

        String normalized = codigo.trim();
        platformModuleRepository.findByCodigoIgnoreCase(normalized)
                .ifPresent(existing -> {
                    throw new ConflictException("Já existe um módulo com este código.");
                });
    }
}
