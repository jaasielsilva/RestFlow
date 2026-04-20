package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantAdvancedBiApiService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/bi-avancado")
@RequiredArgsConstructor
public class TenantAdvancedBiWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final TenantAdvancedBiApiService tenantAdvancedBiApiService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        tenantPortalWebService.requireEnabledModule(authentication, "RELATORIOS");
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "relatorios");
        model.addAttribute("pageTitle", "BI Avançado");
        model.addAttribute("pageSubtitle", "Indicadores executivos da operação");
        model.addAttribute("bi", tenantAdvancedBiApiService.summary(authentication));
        return "tenant/bi/advanced";
    }
}
