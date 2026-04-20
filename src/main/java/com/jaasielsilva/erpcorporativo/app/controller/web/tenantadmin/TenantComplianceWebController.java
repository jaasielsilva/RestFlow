package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantComplianceApiService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/compliance")
@RequiredArgsConstructor
public class TenantComplianceWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final TenantComplianceApiService tenantComplianceApiService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        tenantPortalWebService.requireEnabledModule(authentication, "COMPLIANCE");
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "compliance");
        model.addAttribute("pageTitle", "Compliance / LGPD");
        model.addAttribute("pageSubtitle", "Solicitações de dados e trilha de consentimento");
        model.addAttribute("requests", tenantComplianceApiService.listRequests(authentication));
        model.addAttribute("consents", tenantComplianceApiService.listConsents(authentication));
        return "tenant/compliance/index";
    }
}
