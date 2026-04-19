package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import jakarta.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminTenantCreateForm;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.service.web.admin.AdminTenantWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping({"/admin/tenants", "/admin/tenant"})
@RequiredArgsConstructor
public class AdminTenantWebController {

    private final AdminTenantWebService adminTenantWebService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        model.addAttribute("view", adminTenantWebService.list(nome, slug, ativo, page, size));
        return "admin/tenants/index";
    }

    @GetMapping("/new")
    public String newTenant(Model model) {
        model.addAttribute("form", new AdminTenantCreateForm());
        return "admin/tenants/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") AdminTenantCreateForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/tenants/new";
        }

        try {
            adminTenantWebService.create(form);
            return "redirect:/admin/tenants?created=true";
        } catch (AppException ex) {
            bindingResult.reject("tenant.create", ex.getMessage());
            model.addAttribute("form", form);
            return "admin/tenants/new";
        }
    }

    @PostMapping("/{tenantId}/reset-admin-password")
    public String resetTenantAdminPassword(@PathVariable Long tenantId) {
        try {
            adminTenantWebService.resetTenantAdminPassword(tenantId);
            return "redirect:/admin/tenants?reset=true";
        } catch (AppException ex) {
            return "redirect:/admin/tenants?resetError=true";
        }
    }

    @GetMapping("/{tenantId}/reset-admin-password")
    public String resetTenantAdminPasswordGet(@PathVariable Long tenantId) {
        return resetTenantAdminPassword(tenantId);
    }
}
