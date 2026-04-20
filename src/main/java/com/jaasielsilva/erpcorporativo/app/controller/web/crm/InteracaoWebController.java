package com.jaasielsilva.erpcorporativo.app.controller.web.crm;

import com.jaasielsilva.erpcorporativo.app.dto.web.crm.InteracaoForm;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.usecase.web.crm.InteracaoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/clientes/{clienteId}/interacoes")
@RequiredArgsConstructor
public class InteracaoWebController {

    private final InteracaoUseCase interacaoUseCase;
    private final TenantPortalWebService tenantPortalWebService;

    @PostMapping
    public String create(
            Authentication authentication,
            @PathVariable("clienteId") Long clienteId,
            @Valid @ModelAttribute("interacaoForm") InteracaoForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("toastError", "Dados inválidos. Verifique os campos.");
            return "redirect:/app/clientes/" + clienteId;
        }

        try {
            interacaoUseCase.create(user.getTenantId(), clienteId, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Interação registrada com sucesso.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes/" + clienteId;
    }

    @PostMapping("/{id}")
    public String update(
            Authentication authentication,
            @PathVariable("clienteId") Long clienteId,
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("interacaoForm") InteracaoForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("toastError", "Dados inválidos. Verifique os campos.");
            return "redirect:/app/clientes/" + clienteId;
        }

        try {
            interacaoUseCase.update(user.getTenantId(), id, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Interação atualizada.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes/" + clienteId;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            Authentication authentication,
            @PathVariable("clienteId") Long clienteId,
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");
        try {
            interacaoUseCase.delete(user.getTenantId(), id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Interação removida.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes/" + clienteId;
    }
}
