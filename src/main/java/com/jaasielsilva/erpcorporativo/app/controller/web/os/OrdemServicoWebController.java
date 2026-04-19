package com.jaasielsilva.erpcorporativo.app.controller.web.os;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
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

import com.jaasielsilva.erpcorporativo.app.dto.web.os.OrdemServicoForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.os.OrdemServicoViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.usecase.web.os.OrdemServicoUseCase;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/pedidos")
@RequiredArgsConstructor
public class OrdemServicoWebController {

    private final OrdemServicoUseCase ordemServicoUseCase;
    private final TenantPortalWebService tenantPortalWebService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String index(
            Authentication authentication,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) OrdemServicoStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "PEDIDOS");

        model.addAttribute("view", ordemServicoUseCase.list(user.getTenantId(), titulo, cliente, status, page, size));
        model.addAttribute("statusValues", OrdemServicoStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("canWrite", module.canWrite());
        populateCommon(authentication, model, "pedidos", "Ordens de Serviço", "Gestão de ordens de serviço");
        return "tenant/os/index";
    }

    @GetMapping("/new")
    public String newOs(Authentication authentication, Model model) {
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "PEDIDOS");
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);

        if (!module.canWrite()) {
            return "redirect:/app/pedidos";
        }

        model.addAttribute("form", new OrdemServicoForm());
        model.addAttribute("statusValues", OrdemServicoStatus.values());
        model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
        model.addAttribute("isEdit", false);
        populateCommon(authentication, model, "pedidos", "Nova OS", "Criar ordem de serviço");
        return "tenant/os/form";
    }

    @PostMapping
    public String create(
            Authentication authentication,
            @Valid @ModelAttribute("form") OrdemServicoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("statusValues", OrdemServicoStatus.values());
            model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
            model.addAttribute("isEdit", false);
            populateCommon(authentication, model, "pedidos", "Nova OS", "Criar ordem de serviço");
            return "tenant/os/form";
        }

        try {
            ordemServicoUseCase.create(user.getTenantId(), form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Ordem de serviço criada com sucesso.");
            return "redirect:/app/pedidos";
        } catch (AppException ex) {
            bindingResult.reject("os.create", ex.getMessage());
            model.addAttribute("statusValues", OrdemServicoStatus.values());
            model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
            model.addAttribute("isEdit", false);
            populateCommon(authentication, model, "pedidos", "Nova OS", "Criar ordem de serviço");
            return "tenant/os/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(Authentication authentication, @PathVariable Long id, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "PEDIDOS");

        OrdemServicoViewModel os = ordemServicoUseCase.getById(user.getTenantId(), id);
        model.addAttribute("os", os);
        model.addAttribute("statusValues", OrdemServicoStatus.values());
        model.addAttribute("canWrite", module.canWrite());
        model.addAttribute("canFull", module.canFull());
        populateCommon(authentication, model, "pedidos", "OS " + os.numero(), os.titulo());
        return "tenant/os/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(Authentication authentication, @PathVariable Long id, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "PEDIDOS");

        // USER com READ não pode editar — redireciona para detalhe
        if (!module.canWrite()) {
            return "redirect:/app/pedidos/" + id;
        }

        OrdemServicoViewModel os = ordemServicoUseCase.getById(user.getTenantId(), id);

        OrdemServicoForm form = new OrdemServicoForm();
        form.setTitulo(os.titulo());
        form.setDescricao(os.descricao());
        form.setClienteNome(os.clienteNome());
        form.setClienteEmail(os.clienteEmail());
        form.setClienteTelefone(os.clienteTelefone());
        form.setStatus(os.status());
        form.setValor(os.valor());
        form.setDataPrevista(os.dataPrevista());
        form.setResponsavelId(os.responsavelId());

        model.addAttribute("form", form);
        model.addAttribute("osId", id);
        model.addAttribute("osNumero", os.numero());
        model.addAttribute("statusValues", OrdemServicoStatus.values());
        model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
        model.addAttribute("isEdit", true);
        populateCommon(authentication, model, "pedidos", "Editar OS", os.titulo());
        return "tenant/os/form";
    }

    @PostMapping("/{id}")
    public String update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @ModelAttribute("form") OrdemServicoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("osId", id);
            model.addAttribute("statusValues", OrdemServicoStatus.values());
            model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
            model.addAttribute("isEdit", true);
            populateCommon(authentication, model, "pedidos", "Editar OS", "Atualizar ordem de serviço");
            return "tenant/os/form";
        }

        try {
            ordemServicoUseCase.update(user.getTenantId(), id, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "OS atualizada com sucesso.");
            return "redirect:/app/pedidos/" + id;
        } catch (AppException ex) {
            bindingResult.reject("os.update", ex.getMessage());
            model.addAttribute("osId", id);
            model.addAttribute("statusValues", OrdemServicoStatus.values());
            model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
            model.addAttribute("isEdit", true);
            populateCommon(authentication, model, "pedidos", "Editar OS", "Atualizar ordem de serviço");
            return "tenant/os/form";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam OrdemServicoStatus status,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        try {
            ordemServicoUseCase.updateStatus(user.getTenantId(), id, status);
            redirectAttributes.addFlashAttribute("toastSuccess", "Status atualizado.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/pedidos/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        try {
            ordemServicoUseCase.delete(user.getTenantId(), id);
            redirectAttributes.addFlashAttribute("toastSuccess", "OS removida.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/pedidos";
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
