package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantUserForm;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.user.UsuarioResponse;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantDashboardWebService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantUserWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class TenantDashboardWebController {

    private final TenantDashboardWebService tenantDashboardWebService;
    private final TenantPortalWebService tenantPortalWebService;
    private final TenantUserWebService tenantUserWebService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        populateSidebar(authentication, model);
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        boolean isAdmin = user.getRole() == Role.ADMIN
                || user.getRole() == Role.SUPER_ADMIN;

        TenantPortalModuleViewModel dashModule = tenantPortalWebService.requireEnabledModule(authentication, "DASHBOARD");

        model.addAttribute("activeMenu", tenantPortalWebService.dashboardKey());
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pageSubtitle", isAdmin ? "Visão executiva do seu tenant" : "Visão geral das suas atividades");
        model.addAttribute("view", tenantDashboardWebService.build(authentication));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("canWrite", dashModule.canWrite());
        return "tenant/dashboard/index";
    }

    @GetMapping("/usuarios")
    public String usuarios(
            Authentication authentication,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "USUARIOS");
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", "Usuários");
        model.addAttribute("pageSubtitle", "Gestão de usuários do tenant");
        model.addAttribute("view", tenantUserWebService.list(authentication, nome, email, ativo, role, page, size));
        return "tenant/users/index";
    }

    @GetMapping("/usuarios/new")
    public String newUser(Authentication authentication, Model model) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "USUARIOS");
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", "Novo Usuário");
        model.addAttribute("pageSubtitle", "Criar usuário para o tenant");
        model.addAttribute("form", new TenantUserForm());
        model.addAttribute("isEdit", false);
        return "tenant/users/form";
    }

    @PostMapping("/usuarios")
    public String createUser(
            Authentication authentication,
            @Valid @ModelAttribute("form") TenantUserForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!StringUtils.hasText(form.getPassword())) {
            bindingResult.rejectValue("password", "tenant.user.password.required", "Senha é obrigatória.");
        }

        if (bindingResult.hasErrors()) {
            populateSidebar(authentication, model);
            model.addAttribute("activeMenu", tenantPortalWebService.moduleKey("USUARIOS"));
            model.addAttribute("pageTitle", "Novo Usuário");
            model.addAttribute("pageSubtitle", "Criar usuário para o tenant");
            model.addAttribute("isEdit", false);
            return "tenant/users/form";
        }

        try {
            tenantUserWebService.create(authentication, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Usuário criado com sucesso.");
            return "redirect:/app/usuarios";
        } catch (AppException ex) {
            bindingResult.reject("tenant.user.create", ex.getMessage());
            populateSidebar(authentication, model);
            model.addAttribute("activeMenu", tenantPortalWebService.moduleKey("USUARIOS"));
            model.addAttribute("pageTitle", "Novo Usuário");
            model.addAttribute("pageSubtitle", "Criar usuário para o tenant");
            model.addAttribute("isEdit", false);
            return "tenant/users/form";
        }
    }

    @GetMapping("/usuarios/{id}/edit")
    public String editUser(Authentication authentication, @PathVariable Long id, Model model) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "USUARIOS");
        UsuarioResponse user = tenantUserWebService.getById(authentication, id);

        TenantUserForm form = new TenantUserForm();
        form.setNome(user.nome());
        form.setEmail(user.email());
        form.setAtivo(user.ativo());
        form.setRole(user.role());

        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", "Editar Usuário");
        model.addAttribute("pageSubtitle", "Atualizar dados do usuário");
        model.addAttribute("form", form);
        model.addAttribute("userId", id);
        model.addAttribute("isEdit", true);
        return "tenant/users/form";
    }

    @PostMapping("/usuarios/{id}")
    public String updateUser(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @ModelAttribute("form") TenantUserForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateSidebar(authentication, model);
            model.addAttribute("activeMenu", tenantPortalWebService.moduleKey("USUARIOS"));
            model.addAttribute("pageTitle", "Editar Usuário");
            model.addAttribute("pageSubtitle", "Atualizar dados do usuário");
            model.addAttribute("userId", id);
            model.addAttribute("isEdit", true);
            return "tenant/users/form";
        }

        try {
            tenantUserWebService.update(authentication, id, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Usuário atualizado com sucesso.");
            return "redirect:/app/usuarios";
        } catch (AppException ex) {
            bindingResult.reject("tenant.user.update", ex.getMessage());
            populateSidebar(authentication, model);
            model.addAttribute("activeMenu", tenantPortalWebService.moduleKey("USUARIOS"));
            model.addAttribute("pageTitle", "Editar Usuário");
            model.addAttribute("pageSubtitle", "Atualizar dados do usuário");
            model.addAttribute("userId", id);
            model.addAttribute("isEdit", true);
            return "tenant/users/form";
        }
    }

    @PostMapping("/usuarios/{id}/toggle-status")
    public String toggleUserStatus(Authentication authentication, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            UsuarioResponse updated = tenantUserWebService.toggleActive(authentication, id);
            String message = updated.ativo() ? "Usuário ativado com sucesso." : "Usuário inativado com sucesso.";
            redirectAttributes.addFlashAttribute("toastSuccess", message);
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/usuarios";
    }

    @PostMapping("/usuarios/{id}/reset-password")
    public String resetUserPassword(Authentication authentication, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tenantUserWebService.resetPassword(authentication, id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Senha resetada para: mudar123");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/usuarios";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Authentication authentication, Model model) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "CONFIGURACOES");
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", "Configurações");
        model.addAttribute("pageSubtitle", "Preferências e regras do tenant");
        model.addAttribute("moduleName", "Configurações");
        return "tenant/placeholder/index";
    }

    @GetMapping("/modulos/{codigo}")
    public String modulePage(
            Authentication authentication,
            @PathVariable String codigo,
            Model model
    ) {
        populateSidebar(authentication, model);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, codigo);
        model.addAttribute("activeMenu", module.activeKey());
        model.addAttribute("pageTitle", module.nome());
        model.addAttribute("pageSubtitle", "Módulo habilitado para o seu tenant");
        model.addAttribute("moduleName", module.nome());
        return "tenant/placeholder/index";
    }

    private void populateSidebar(Authentication authentication, Model model) {
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
    }
}
