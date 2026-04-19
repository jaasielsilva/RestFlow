package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminPlanCreateForm;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.web.admin.AdminPermissionsWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionsWebController {

    private final AdminPermissionsWebService adminPermissionsWebService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("activeMenu", "permissions");
        model.addAttribute("pageTitle", "Permissões");
        model.addAttribute("pageSubtitle", "Planos de assinatura e controle de acesso por tenant");
        model.addAttribute("view", adminPermissionsWebService.buildViewModel());
        model.addAttribute("form", new AdminPlanCreateForm());
        return "admin/permissions/index";
    }

    @PostMapping("/plans")
    public String createPlan(
            @Valid @ModelAttribute("form") AdminPlanCreateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "permissions");
            model.addAttribute("pageTitle", "Permissões");
            model.addAttribute("pageSubtitle", "Planos de assinatura e controle de acesso por tenant");
            model.addAttribute("view", adminPermissionsWebService.buildViewModel());
            return "admin/permissions/index";
        }

        try {
            adminPermissionsWebService.createPlan(form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Plano criado com sucesso.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/admin/permissions";
    }

    @PostMapping("/tenants/{tenantId}/assign")
    public String assignPlan(
            @PathVariable Long tenantId,
            @RequestParam Long planId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminPermissionsWebService.assignPlanToTenant(tenantId, planId);
            redirectAttributes.addFlashAttribute("toastSuccess", "Plano atribuído ao tenant com sucesso. Módulos provisionados.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/admin/permissions";
    }

    @GetMapping("/tenants/{tenantId}/matrix")
    public String matrix(@PathVariable Long tenantId, Model model) {
        model.addAttribute("activeMenu", "permissions");
        model.addAttribute("pageTitle", "Permissões");
        model.addAttribute("pageSubtitle", "Matriz de acesso por role e módulo");
        model.addAttribute("matrix", adminPermissionsWebService.buildMatrix(tenantId));
        model.addAttribute("roles", new Role[]{Role.ADMIN, Role.USER});
        return "admin/permissions/matrix";
    }

    @PostMapping("/tenants/{tenantId}/matrix")
    public String saveMatrix(
            @PathVariable Long tenantId,
            @RequestParam Long moduleId,
            @RequestParam Role role,
            @RequestParam AccessLevel accessLevel,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminPermissionsWebService.setPermission(tenantId, moduleId, role, accessLevel);
            redirectAttributes.addFlashAttribute("toastSuccess", "Permissão atualizada.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/admin/permissions/tenants/" + tenantId + "/matrix";
    }
}
