package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.TenantModule;
import com.jaasielsilva.erpcorporativo.app.repository.module.TenantModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.permission.TenantRolePermissionRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.shared.ModuleVisualMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantPortalWebService {

    private static final String DASHBOARD_CODE    = "DASHBOARD";
    private static final String SETTINGS_CODE     = "CONFIGURACOES";

    private final TenantModuleRepository tenantModuleRepository;
    private final TenantRolePermissionRepository permissionRepository;
    private final ModuleVisualMapper moduleVisualMapper;

    public List<TenantPortalModuleViewModel> listEnabledModules(Authentication authentication) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);

        List<TenantPortalModuleViewModel> enabledModules = tenantModuleRepository
                .findEnabledModulesByTenantId(currentUser.getTenantId())
                .stream()
                .map(tm -> toViewModel(tm, currentUser))
                .filter(m -> !m.codigo().equalsIgnoreCase(DASHBOARD_CODE))
                // Filtra módulos com NONE para a role do usuário (exceto ADMIN que sempre vê tudo)
                .filter(m -> currentUser.getRole() == Role.ADMIN || m.canRead())
                .toList();

        List<TenantPortalModuleViewModel> modules = new ArrayList<>();
        modules.add(dashboardModule());
        modules.addAll(enabledModules);
        return modules;
    }

    public TenantPortalModuleViewModel requireEnabledModule(Authentication authentication, String codigo) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);

        if (DASHBOARD_CODE.equalsIgnoreCase(codigo)) {
            return dashboardModule();
        }
        if (SETTINGS_CODE.equalsIgnoreCase(codigo)) {
            return fallbackModule(SETTINGS_CODE, AccessLevel.FULL);
        }

        boolean enabled = tenantModuleRepository.hasEnabledModuleByCodigo(currentUser.getTenantId(), codigo);
        if (!enabled) {
            throw new AccessDeniedException("Módulo não habilitado para este tenant.");
        }

        // Verifica AccessLevel para a role do usuário (ADMIN sempre tem FULL)
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            Long moduleId = tenantModuleRepository.findEnabledModulesByTenantId(currentUser.getTenantId())
                    .stream()
                    .filter(tm -> tm.getModule().getCodigo().equalsIgnoreCase(codigo))
                    .map(tm -> tm.getModule().getId())
                    .findFirst()
                    .orElse(null);

            if (moduleId != null) {
                AccessLevel level = permissionRepository
                        .findAccessLevel(currentUser.getTenantId(), moduleId, currentUser.getRole())
                        .orElse(AccessLevel.NONE);

                if (level == AccessLevel.NONE) {
                    throw new AccessDeniedException("Sem permissão de acesso a este módulo.");
                }
            }
        }

        return listEnabledModules(authentication).stream()
                .filter(m -> m.codigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseGet(() -> fallbackModule(codigo, AccessLevel.FULL));
    }

    public String dashboardKey() {
        return "dashboard";
    }

    public String moduleKey(String codigo) {
        return normalizeCode(codigo);
    }

    // -------------------------------------------------------------------------

    private TenantPortalModuleViewModel toViewModel(TenantModule tenantModule, AppUserDetails currentUser) {
        String codigo     = tenantModule.getModule().getCodigo();
        String normalized = normalizeCode(codigo);
        String rota       = resolveRota(tenantModule.getModule(), normalized);
        AccessLevel level = resolveAccessLevel(currentUser, tenantModule.getModule().getId());

        return new TenantPortalModuleViewModel(
                codigo,
                tenantModule.getModule().getNome(),
                rota,
                moduleVisualMapper.iconClass(codigo),
                moduleVisualMapper.toneClass(codigo),
                normalized,
                level
        );
    }

    /**
     * Resolve a rota do módulo:
     * 1. Usa a rota configurada no banco (campo rota do PlatformModule) se existir
     * 2. Caso contrário, gera automaticamente como /app/modulos/{codigo_lowercase}
     * O DASHBOARD é sempre /app independente de configuração.
     */
    private String resolveRota(com.jaasielsilva.erpcorporativo.app.model.PlatformModule module, String normalized) {
        if (DASHBOARD_CODE.equalsIgnoreCase(module.getCodigo())) {
            return "/app";
        }
        if (module.getRota() != null && !module.getRota().isBlank()) {
            return module.getRota().trim();
        }
        return "/app/modulos/" + normalized;
    }

    private AccessLevel resolveAccessLevel(AppUserDetails user, Long moduleId) {
        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.ADMIN) {
            return AccessLevel.FULL;
        }
        return permissionRepository
                .findAccessLevel(user.getTenantId(), moduleId, user.getRole())
                .orElse(AccessLevel.NONE);
    }

    private TenantPortalModuleViewModel fallbackModule(String codigo, AccessLevel level) {
        String normalized = normalizeCode(codigo);
        return new TenantPortalModuleViewModel(
                codigo,
                codigo,
                "/app/modulos/" + normalized,
                moduleVisualMapper.iconClass(codigo),
                moduleVisualMapper.toneClass(codigo),
                normalized,
                level
        );
    }

    private TenantPortalModuleViewModel dashboardModule() {
        return new TenantPortalModuleViewModel(
                DASHBOARD_CODE,
                "Dashboard",
                "/app",
                moduleVisualMapper.iconClass(DASHBOARD_CODE),
                moduleVisualMapper.toneClass(DASHBOARD_CODE),
                dashboardKey(),
                AccessLevel.FULL
        );
    }

    private String normalizeCode(String codigo) {
        if (codigo == null) return "";
        return codigo.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
