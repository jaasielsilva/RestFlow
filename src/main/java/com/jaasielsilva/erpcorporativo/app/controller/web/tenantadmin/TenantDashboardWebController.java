package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantUserWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class TenantDashboardWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final TenantUserWebService tenantUserWebService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        populateSidebar(authentication, model);
        model.addAttribute("activeMenu", tenantPortalWebService.dashboardKey());
        model.addAttribute("pageTitle", "Dashboard do Cliente");
        model.addAttribute("pageSubtitle", "Visão inicial do seu tenant");
        return "tenant/dashboard/index";
    }

    @GetMapping("/usuarios")
    public String usuarios(
            Authentication authentication,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "USUARIOS");
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", "Usuários");
        model.addAttribute("pageSubtitle", "Gestão de usuários do tenant");
        model.addAttribute("view", tenantUserWebService.list(authentication, nome, email, ativo, role, page, size));
        return "tenant/users/index";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Authentication authentication, Model model) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "CONFIGURACOES");
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", "Configurações");
        model.addAttribute("pageSubtitle", "Preferências e regras do tenant");
        model.addAttribute("moduleName", "Configurações");
        return "tenant/placeholder/index";
    }

    @GetMapping("/modulos/{codigo}")
    public String modulePage(
            Authentication authentication,
            @PathVariable String codigo,
            Model model
    ) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, codigo);
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", module.nome());
        model.addAttribute("pageSubtitle", "Módulo habilitado para o seu tenant");
        model.addAttribute("moduleName", module.nome());
        return "tenant/placeholder/index";
    }

    private void populateSidebar(Authentication authentication, Model model) {
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
    }
}
