package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import jakarta.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminTenantCreateForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminTenantUpdateForm;
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
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "slug", required = false) String slug,
            @RequestParam(name = "ativo", required = false) Boolean ativo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
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
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/tenants/new";
        }

        try {
            adminTenantWebService.create(form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Tenant criado com sucesso.");
            return "redirect:/admin/tenants";
        } catch (AppException ex) {
            bindingResult.reject("tenant.create", ex.getMessage());
            model.addAttribute("form", form);
            return "admin/tenants/new";
        }
    }

    @GetMapping("/{tenantId}/edit")
    public String editTenant(@PathVariable("tenantId") Long tenantId, Model model) {
        model.addAttribute("form", adminTenantWebService.getUpdateForm(tenantId));
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("pageTitle", "Editar Tenant");
        model.addAttribute("pageSubtitle", "Atualize nome, slug e status do tenant");
        return "admin/tenants/edit";
    }

    @PostMapping("/{tenantId}/edit")
    public String updateTenant(
            @PathVariable("tenantId") Long tenantId,
            @Valid @ModelAttribute("form") AdminTenantUpdateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("pageTitle", "Editar Tenant");
            model.addAttribute("pageSubtitle", "Atualize nome, slug e status do tenant");
            return "admin/tenants/edit";
        }

        try {
            adminTenantWebService.update(tenantId, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Tenant atualizado com sucesso.");
            return "redirect:/admin/tenants";
        } catch (AppException ex) {
            bindingResult.reject("tenant.update", ex.getMessage());
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("pageTitle", "Editar Tenant");
            model.addAttribute("pageSubtitle", "Atualize nome, slug e status do tenant");
            return "admin/tenants/edit";
        }
    }

    @PostMapping("/{tenantId}/toggle-status")
    public String toggleTenantStatus(@PathVariable("tenantId") Long tenantId, RedirectAttributes redirectAttributes) {
        try {
            boolean ativo = adminTenantWebService.toggleStatus(tenantId);
            String message = ativo ? "Tenant ativado com sucesso." : "Tenant inativado com sucesso.";
            redirectAttributes.addFlashAttribute("toastSuccess", message);
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/tenants";
    }

    @PostMapping("/{tenantId}/reset-admin-password")
    public String resetTenantAdminPassword(@PathVariable("tenantId") Long tenantId, RedirectAttributes redirectAttributes) {
        try {
            var result = adminTenantWebService.resetTenantAdminPassword(tenantId);
            redirectAttributes.addFlashAttribute(
                    "toastSuccess",
                    "Senha resetada para: " + result.generatedPassword()
            );
            return "redirect:/admin/tenants";
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
            return "redirect:/admin/tenants";
        }
    }

    @GetMapping("/{tenantId}/reset-admin-password")
    public String resetTenantAdminPasswordGet(@PathVariable("tenantId") Long tenantId, RedirectAttributes redirectAttributes) {
        return resetTenantAdminPassword(tenantId, redirectAttributes);
    }
}
