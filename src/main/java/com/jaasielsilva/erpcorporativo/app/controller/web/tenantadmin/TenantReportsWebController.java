package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantReportsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/modulos/relatorios")
@RequiredArgsConstructor
public class TenantReportsWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final TenantReportsService tenantReportsService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        var user = SecurityPrincipalUtils.getCurrentUser(authentication);
        
        TenantPortalModuleViewModel module =
        tenantPortalWebService.requireEnabledModule(authentication, "RELATORIOS");

        var dashboard = tenantReportsService.getResumo(user.getTenantId());

        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", module.nome());
        model.addAttribute("pageSubtitle", "Relatórios e indicadores do tenant");
        model.addAttribute("dashboard", dashboard);

        return "tenant/reports/index";
    }
}