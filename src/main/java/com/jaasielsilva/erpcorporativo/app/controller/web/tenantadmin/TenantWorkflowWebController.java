package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantWorkflowApiService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/automacoes")
@RequiredArgsConstructor
public class TenantWorkflowWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final TenantWorkflowApiService tenantWorkflowApiService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        tenantPortalWebService.requireEnabledModule(authentication, "AUTOMACOES");
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "automacoes");
        model.addAttribute("pageTitle", "Automações de Workflow");
        model.addAttribute("pageSubtitle", "Regras inteligentes e logs de execução");
        model.addAttribute("rules", tenantWorkflowApiService.list(authentication));
        model.addAttribute("logs", tenantWorkflowApiService.logs(authentication));
        return "tenant/workflows/index";
    }
}
