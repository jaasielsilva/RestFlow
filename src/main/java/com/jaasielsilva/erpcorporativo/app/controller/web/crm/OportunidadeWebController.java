package com.jaasielsilva.erpcorporativo.app.controller.web.crm;

import com.jaasielsilva.erpcorporativo.app.dto.web.crm.OportunidadeForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.StatusOportunidade;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.usecase.web.crm.OportunidadeUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/clientes/oportunidades")
@RequiredArgsConstructor
public class OportunidadeWebController {

    private final OportunidadeUseCase oportunidadeUseCase;
    private final TenantPortalWebService tenantPortalWebService;

    @GetMapping
    public String funil(Authentication authentication, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        model.addAttribute("view", oportunidadeUseCase.funil(user.getTenantId()));
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new OportunidadeForm());
        }
        model.addAttribute("statusValues", StatusOportunidade.values());
        model.addAttribute("canWrite", module.canWrite());
        populateCommon(authentication, model, "clientes", "Funil de Oportunidades", "Gestão de oportunidades");
        return "tenant/crm/oportunidades";
    }

    @PostMapping
    public String create(
            Authentication authentication,
            @Valid @ModelAttribute("form") OportunidadeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        if (bindingResult.hasErrors()) {
            model.addAttribute("view", oportunidadeUseCase.funil(user.getTenantId()));
            model.addAttribute("statusValues", StatusOportunidade.values());
            model.addAttribute("canWrite", true);
            model.addAttribute("form", form);
            populateCommon(authentication, model, "clientes", "Funil de Oportunidades", "Gestão de oportunidades");
            return "tenant/crm/oportunidades";
        }

        try {
            oportunidadeUseCase.create(user.getTenantId(), form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Oportunidade criada com sucesso.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes/oportunidades";
    }

    @PostMapping("/{id}")
    public String update(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("form") OportunidadeForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("toastError", "Dados inválidos. Verifique os campos.");
            return "redirect:/app/clientes/oportunidades";
        }

        try {
            oportunidadeUseCase.update(user.getTenantId(), id, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Oportunidade atualizada.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes/oportunidades";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            Authentication authentication,
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");
        try {
            oportunidadeUseCase.delete(user.getTenantId(), id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Oportunidade removida.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes/oportunidades";
    }

    // -------------------------------------------------------------------------

    private void populateCommon(Authentication auth, Model model, String activeMenu,
                                 String pageTitle, String pageSubtitle) {
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(auth));
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageSubtitle", pageSubtitle);
    }
}
