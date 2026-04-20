package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantIntegrationApiService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/integracoes")
@RequiredArgsConstructor
public class TenantIntegrationWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final TenantIntegrationApiService tenantIntegrationApiService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        tenantPortalWebService.requireEnabledModule(authentication, "INTEGRACOES");
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "integracoes");
        model.addAttribute("pageTitle", "Integrações e Webhooks");
        model.addAttribute("pageSubtitle", "Endpoints externos e logs de entrega");
        model.addAttribute("endpoints", tenantIntegrationApiService.list(authentication));
        model.addAttribute("logs", tenantIntegrationApiService.logs(authentication));
        return "tenant/integrations/index";
    }
}
