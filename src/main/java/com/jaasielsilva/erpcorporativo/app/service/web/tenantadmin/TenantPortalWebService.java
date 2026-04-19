package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.model.TenantModule;
import com.jaasielsilva.erpcorporativo.app.repository.module.TenantModuleRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantPortalWebService {

    private static final String DASHBOARD_CODE = "DASHBOARD";
    private static final String USUARIOS_CODE = "USUARIOS";
    private static final String CONFIGURACOES_CODE = "CONFIGURACOES";
    private static final String PEDIDOS_CODE = "PEDIDOS";
    private static final String ESTOQUE_CODE = "ESTOQUE";
    private static final String FINANCEIRO_CODE = "FINANCEIRO";
    private static final String RELATORIOS_CODE = "RELATORIOS";

    private final TenantModuleRepository tenantModuleRepository;

    public List<TenantPortalModuleViewModel> listEnabledModules(Authentication authentication) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);

        List<TenantPortalModuleViewModel> enabledModules = tenantModuleRepository
                .findEnabledModulesByTenantId(currentUser.getTenantId())
                .stream()
                .map(this::toViewModel)
                .filter(module -> !module.codigo().equalsIgnoreCase(DASHBOARD_CODE))
                .toList();

        List<TenantPortalModuleViewModel> modules = new ArrayList<>();
        modules.add(dashboardModule());
        modules.addAll(enabledModules);
        return modules;
    }

    public TenantPortalModuleViewModel requireEnabledModule(Authentication authentication, String codigo) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);

        boolean enabled = tenantModuleRepository.hasEnabledModuleByCodigo(
                currentUser.getTenantId(),
                codigo
        );
        if (!enabled) {
            throw new AccessDeniedException("Módulo não habilitado para este tenant.");
        }

        return listEnabledModules(authentication).stream()
                .filter(module -> module.codigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseGet(() -> fallbackModule(codigo));
    }

    public String dashboardKey() {
        return "dashboard";
    }

    private TenantPortalModuleViewModel toViewModel(TenantModule tenantModule) {
        String codigo = tenantModule.getModule().getCodigo();
        String normalized = normalizeCode(codigo);
        return new TenantPortalModuleViewModel(
                codigo,
                tenantModule.getModule().getNome(),
                resolvePath(codigo, normalized),
                resolveIcon(codigo),
                normalized
        );
    }

    private TenantPortalModuleViewModel fallbackModule(String codigo) {
        String normalized = normalizeCode(codigo);
        return new TenantPortalModuleViewModel(
                codigo,
                codigo,
                resolvePath(codigo, normalized),
                resolveIcon(codigo),
                normalized
        );
    }

    private String resolvePath(String codigo, String normalized) {
        if (DASHBOARD_CODE.equalsIgnoreCase(codigo)) {
            return "/app";
        }

        if (USUARIOS_CODE.equalsIgnoreCase(codigo)) {
            return "/app/usuarios";
        }

        if (CONFIGURACOES_CODE.equalsIgnoreCase(codigo)) {
            return "/app/configuracoes";
        }

        if (PEDIDOS_CODE.equalsIgnoreCase(codigo)) {
            return "/app/modulos/pedidos";
        }

        if (ESTOQUE_CODE.equalsIgnoreCase(codigo)) {
            return "/app/modulos/estoque";
        }

        if (FINANCEIRO_CODE.equalsIgnoreCase(codigo)) {
            return "/app/modulos/financeiro";
        }

        if (RELATORIOS_CODE.equalsIgnoreCase(codigo)) {
            return "/app/modulos/relatorios";
        }

        return "/app/modulos/" + normalized;
    }

    private String resolveIcon(String codigo) {
        if (DASHBOARD_CODE.equalsIgnoreCase(codigo)) {
            return "fa-solid fa-house";
        }

        if (USUARIOS_CODE.equalsIgnoreCase(codigo)) {
            return "fa-solid fa-users";
        }

        if (CONFIGURACOES_CODE.equalsIgnoreCase(codigo)) {
            return "fa-solid fa-gear";
        }

        if (PEDIDOS_CODE.equalsIgnoreCase(codigo)) {
            return "fa-solid fa-receipt";
        }

        if (ESTOQUE_CODE.equalsIgnoreCase(codigo)) {
            return "fa-solid fa-boxes-stacked";
        }

        if (FINANCEIRO_CODE.equalsIgnoreCase(codigo)) {
            return "fa-solid fa-wallet";
        }

        if (RELATORIOS_CODE.equalsIgnoreCase(codigo)) {
            return "fa-solid fa-chart-column";
        }

        return "fa-solid fa-puzzle-piece";
    }

    private TenantPortalModuleViewModel dashboardModule() {
        return new TenantPortalModuleViewModel(
                DASHBOARD_CODE,
                "Dashboard",
                "/app",
                "fa-solid fa-house",
                dashboardKey()
        );
    }

    private String normalizeCode(String codigo) {
        if (codigo == null) {
            return "";
        }
        return codigo.trim().toLowerCase(Locale.ROOT);
    }
}
