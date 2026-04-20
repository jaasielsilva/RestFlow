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
        populateCommon(model, "Permissões", "Governança de planos, atribuições e matriz de acesso");
        model.addAttribute("view", adminPermissionsWebService.buildViewModel());
        return "admin/permissions/overview";
    }

    @GetMapping("/plans")
    public String plans(Model model) {
        populateCommon(model, "Planos", "Catálogo de planos e componentes");
        model.addAttribute("view", adminPermissionsWebService.buildViewModel());
        return "admin/permissions/plans";
    }

    @GetMapping("/plans/new")
    public String newPlan(Model model) {
        populateCommon(model, "Novo Plano", "Criação de plano de assinatura");
        model.addAttribute("view", adminPermissionsWebService.buildViewModel());
        model.addAttribute("form", new AdminPlanCreateForm());
        return "admin/permissions/plan-form";
    }

    @GetMapping("/assign")
    public String assign(Model model) {
        populateCommon(model, "Atribuições", "Vincule planos aos tenants ativos");
        model.addAttribute("view", adminPermissionsWebService.buildViewModel());
        return "admin/permissions/assign";
    }

    @PostMapping("/plans")
    public String createPlan(
            @Valid @ModelAttribute("form") AdminPlanCreateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateCommon(model, "Novo Plano", "Criação de plano de assinatura");
            model.addAttribute("view", adminPermissionsWebService.buildViewModel());
            return "admin/permissions/plan-form";
        }

        try {
            adminPermissionsWebService.createPlan(form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Plano criado com sucesso.");
            return "redirect:/admin/permissions/plans";
        } catch (AppException ex) {
            model.addAttribute("toastError", ex.getMessage());
            populateCommon(model, "Novo Plano", "Criação de plano de assinatura");
            model.addAttribute("view", adminPermissionsWebService.buildViewModel());
            return "admin/permissions/plan-form";
        }
    }

    @PostMapping("/tenants/{tenantId}/assign")
    public String assignPlan(
            @PathVariable("tenantId") Long tenantId,
            @RequestParam("planId") Long planId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminPermissionsWebService.assignPlanToTenant(tenantId, planId);
            redirectAttributes.addFlashAttribute("toastSuccess", "Plano atribuído ao tenant com sucesso. Módulos provisionados.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/admin/permissions/assign";
    }

    @GetMapping("/tenants/{tenantId}/matrix")
    public String matrix(@PathVariable("tenantId") Long tenantId, Model model) {
        model.addAttribute("activeMenu", "permissions");
        model.addAttribute("pageTitle", "Permissões");
        model.addAttribute("pageSubtitle", "Matriz de acesso por role e módulo");
        model.addAttribute("matrix", adminPermissionsWebService.buildMatrix(tenantId));
        model.addAttribute("roles", new Role[]{Role.ADMIN, Role.USER});
        return "admin/permissions/matrix";
    }

    @PostMapping("/tenants/{tenantId}/matrix")
    public String saveMatrix(
            @PathVariable("tenantId") Long tenantId,
            @RequestParam("moduleId") Long moduleId,
            @RequestParam("role") Role role,
            @RequestParam("accessLevel") AccessLevel accessLevel,
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

    private void populateCommon(Model model, String title, String subtitle) {
        model.addAttribute("activeMenu", "permissions");
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageSubtitle", subtitle);
    }
}
