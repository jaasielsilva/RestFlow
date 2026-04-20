package com.jaasielsilva.erpcorporativo.app.controller.web.crm;

import com.jaasielsilva.erpcorporativo.app.dto.web.crm.ClienteForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.crm.ClienteViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.Genero;
import com.jaasielsilva.erpcorporativo.app.model.StatusCliente;
import com.jaasielsilva.erpcorporativo.app.model.TipoCliente;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.usecase.web.crm.ClienteUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app/clientes")
@RequiredArgsConstructor
public class ClienteWebController {

    private final ClienteUseCase clienteUseCase;
    private final TenantPortalWebService tenantPortalWebService;

    @GetMapping
    public String index(
            Authentication authentication,
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "tipo", required = false) TipoCliente tipo,
            @RequestParam(name = "status", required = false) StatusCliente status,
            @RequestParam(name = "documento", required = false) String documento,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Model model
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        model.addAttribute("view", clienteUseCase.list(user.getTenantId(), nome, tipo, status, documento, page, size));
        model.addAttribute("tipoValues", TipoCliente.values());
        model.addAttribute("statusValues", StatusCliente.values());
        model.addAttribute("selectedTipo", tipo);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("canWrite", module.canWrite());
        populateCommon(authentication, model, "clientes", "Clientes", "Gestão de clientes");
        return "tenant/crm/index";
    }

    @GetMapping("/new")
    public String newCliente(Authentication authentication, Model model) {
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");
        if (!module.canWrite()) return "redirect:/app/clientes";

        model.addAttribute("form", new ClienteForm());
        populateFormModel(model, false, null, null);
        populateCommon(authentication, model, "clientes", "Novo Cliente", "Cadastrar cliente");
        return "tenant/crm/form";
    }

    @PostMapping
    public String create(
            Authentication authentication,
            @Valid @ModelAttribute("form") ClienteForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        if (bindingResult.hasErrors()) {
            populateFormModel(model, false, null, null);
            populateCommon(authentication, model, "clientes", "Novo Cliente", "Cadastrar cliente");
            return "tenant/crm/form";
        }

        try {
            clienteUseCase.create(user.getTenantId(), form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Cliente cadastrado com sucesso.");
            return "redirect:/app/clientes";
        } catch (AppException ex) {
            bindingResult.reject("cliente.create", ex.getMessage());
            populateFormModel(model, false, null, null);
            populateCommon(authentication, model, "clientes", "Novo Cliente", "Cadastrar cliente");
            return "tenant/crm/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(Authentication authentication, @PathVariable("id") Long id, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        ClienteViewModel cliente = clienteUseCase.getById(user.getTenantId(), id);
        model.addAttribute("cliente", cliente);
        model.addAttribute("statusValues", StatusCliente.values());
        model.addAttribute("canWrite", module.canWrite());
        model.addAttribute("canFull", module.canFull());
        populateCommon(authentication, model, "clientes", cliente.numero() + " — " + cliente.nome(), "Detalhe do cliente");
        return "tenant/crm/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(Authentication authentication, @PathVariable("id") Long id, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        if (!module.canWrite()) return "redirect:/app/clientes/" + id;

        ClienteViewModel cliente = clienteUseCase.getById(user.getTenantId(), id);
        ClienteForm form = toForm(cliente);

        model.addAttribute("form", form);
        populateFormModel(model, true, id, cliente.numero());
        populateCommon(authentication, model, "clientes", "Editar " + cliente.numero(), cliente.nome());
        return "tenant/crm/form";
    }

    @PostMapping("/{id}")
    public String update(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("form") ClienteForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");

        if (bindingResult.hasErrors()) {
            populateFormModel(model, true, id, null);
            populateCommon(authentication, model, "clientes", "Editar Cliente", "Atualizar cliente");
            return "tenant/crm/form";
        }

        try {
            clienteUseCase.update(user.getTenantId(), id, form);
            redirectAttributes.addFlashAttribute("toastSuccess", "Cliente atualizado com sucesso.");
            return "redirect:/app/clientes/" + id;
        } catch (AppException ex) {
            bindingResult.reject("cliente.update", ex.getMessage());
            populateFormModel(model, true, id, null);
            populateCommon(authentication, model, "clientes", "Editar Cliente", "Atualizar cliente");
            return "tenant/crm/form";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestParam("status") StatusCliente status,
            @RequestParam(name = "motivoBloqueio", required = false) String motivoBloqueio,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "CLIENTES");
        try {
            clienteUseCase.updateStatus(user.getTenantId(), id, status, motivoBloqueio);
            redirectAttributes.addFlashAttribute("toastSuccess", "Status atualizado para " + status + ".");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes/" + id;
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
            clienteUseCase.delete(user.getTenantId(), id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Cliente removido.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/clientes";
    }

    // -------------------------------------------------------------------------

    private void populateFormModel(Model model, boolean isEdit, Long clienteId, String clienteNumero) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("clienteNumero", clienteNumero);
        model.addAttribute("tipoValues", TipoCliente.values());
        model.addAttribute("statusValues", StatusCliente.values());
        model.addAttribute("generoValues", Genero.values());
    }

    private void populateCommon(Authentication auth, Model model, String activeMenu,
                                 String pageTitle, String pageSubtitle) {
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(auth));
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageSubtitle", pageSubtitle);
    }

    private ClienteForm toForm(ClienteViewModel c) {
        ClienteForm form = new ClienteForm();
        form.setTipo(c.tipo());
        form.setNome(c.nome());
        form.setDocumento(c.documento());
        form.setEmail(c.email());
        form.setTelefonePrincipal(c.telefonePrincipal());
        form.setTelefoneSecundario(c.telefoneSecundario());
        form.setLogradouro(c.logradouro());
        form.setNumeroEndereco(c.numeroEndereco());
        form.setComplemento(c.complemento());
        form.setBairro(c.bairro());
        form.setCidade(c.cidade());
        form.setEstado(c.estado());
        form.setCep(c.cep());
        form.setStatus(c.status());
        form.setObservacoes(c.observacoes());
        form.setDataNascimento(c.dataNascimento());
        form.setGenero(c.genero());
        form.setNomeFantasia(c.nomeFantasia());
        form.setInscricaoEstadual(c.inscricaoEstadual());
        form.setContatoPrincipal(c.contatoPrincipal());
        form.setMotivoBloqueio(c.motivoBloqueio());
        return form;
    }
}
