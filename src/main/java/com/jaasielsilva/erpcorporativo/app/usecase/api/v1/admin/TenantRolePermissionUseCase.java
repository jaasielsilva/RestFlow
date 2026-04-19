package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.TenantRolePermission;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.module.TenantModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.permission.TenantRolePermissionRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantRolePermissionUseCase {

    private final TenantRolePermissionRepository permissionRepository;
    private final TenantRepository tenantRepository;
    private final PlatformModuleRepository platformModuleRepository;
    private final TenantModuleRepository tenantModuleRepository;

    @Transactional(readOnly = true)
    public List<TenantRolePermission> listByTenant(Long tenantId) {
        return permissionRepository.findAllByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<TenantRolePermission> listByTenantAndRole(Long tenantId, Role role) {
        return permissionRepository.findAllByTenantIdAndRole(tenantId, role);
    }

    /**
     * Define ou atualiza o nível de acesso de uma role a um módulo dentro de um tenant.
     * O módulo precisa estar habilitado para o tenant.
     */
    @Transactional
    public TenantRolePermission setPermission(Long tenantId, Long moduleId, Role role, AccessLevel accessLevel) {
        if (role == Role.SUPER_ADMIN) {
            throw new ValidationException("Não é permitido definir permissões para SUPER_ADMIN.");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + tenantId));

        PlatformModule module = platformModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado: " + moduleId));

        boolean moduleEnabled = tenantModuleRepository.hasEnabledModuleByCodigo(tenantId, module.getCodigo());
        if (!moduleEnabled) {
            throw new ValidationException("O módulo '" + module.getNome() + "' não está habilitado para este tenant.");
        }

        TenantRolePermission permission = permissionRepository
                .findByTenantIdAndModuleIdAndRole(tenantId, moduleId, role)
                .orElseGet(() -> TenantRolePermission.builder()
                        .tenant(tenant)
                        .module(module)
                        .role(role)
                        .accessLevel(AccessLevel.NONE)
                        .build());

        permission.setAccessLevel(accessLevel);
        return permissionRepository.save(permission);
    }

    /**
     * Verifica o nível de acesso de uma role a um módulo. Retorna NONE se não configurado.
     * ADMIN sempre recebe FULL por padrão se não houver configuração explícita.
     */
    @Transactional(readOnly = true)
    public AccessLevel getAccessLevel(Long tenantId, Long moduleId, Role role) {
        if (role == Role.SUPER_ADMIN) {
            return AccessLevel.FULL;
        }

        return permissionRepository.findAccessLevel(tenantId, moduleId, role)
                .orElse(role == Role.ADMIN ? AccessLevel.FULL : AccessLevel.NONE);
    }
}
