package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminModuleCreateForm;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.service.web.admin.AdminPlatformModuleWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/modulos")
@RequiredArgsConstructor
public class PlatformModulesWebController {

    private final AdminPlatformModuleWebService adminPlatformModuleWebService;

    @GetMapping
    public String index(Model model) {
        List<PlatformModule> modules = adminPlatformModuleWebService.listModules();
        model.addAttribute("modules", modules);
        model.addAttribute("form", new AdminModuleCreateForm());
        return "admin/modules/index";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") AdminModuleCreateForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modules", adminPlatformModuleWebService.listModules());
            return "admin/modules/index";
        }

        try {
            adminPlatformModuleWebService.create(form);
            return "redirect:/admin/modulos?created=true";
        } catch (AppException ex) {
            bindingResult.reject("module.create", ex.getMessage());
            model.addAttribute("modules", adminPlatformModuleWebService.listModules());
            return "admin/modules/index";
        }
    }

    @GetMapping("/tenants/{tenantId}")
    public String tenantModules(@PathVariable Long tenantId, Model model) {
        Tenant tenant = adminPlatformModuleWebService.getTenant(tenantId);
        List<PlatformModule> modules = adminPlatformModuleWebService.listModules();
        Map<Long, Boolean> states = adminPlatformModuleWebService.getTenantModuleStates(tenantId);

        model.addAttribute("tenant", tenant);
        model.addAttribute("modules", modules);
        model.addAttribute("states", states);
        return "admin/modules/tenant";
    }

    @PostMapping("/tenants/{tenantId}")
    public String setTenantModule(
            @PathVariable Long tenantId,
            @RequestParam Long moduleId,
            @RequestParam boolean enabled
    ) {
        adminPlatformModuleWebService.setTenantModule(tenantId, moduleId, enabled);
        return "redirect:/admin/modulos/tenants/" + tenantId + "?updated=true";
    }
}
