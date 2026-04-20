package com.jaasielsilva.erpcorporativo.app.controller.web.support;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportSlaPolicyForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketMessageForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.TenantPortalModuleViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.Cliente;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteRepository;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;
import com.jaasielsilva.erpcorporativo.app.service.shared.SupportAttachmentStorageService;
import com.jaasielsilva.erpcorporativo.app.service.shared.SupportKnowledgeSuggestionService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;
import com.jaasielsilva.erpcorporativo.app.usecase.web.support.SupportDashboardUseCase;
import com.jaasielsilva.erpcorporativo.app.usecase.web.support.SupportTicketUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/suporte")
@RequiredArgsConstructor
public class SupportWebController {

    private final SupportTicketUseCase supportTicketUseCase;
    private final TenantPortalWebService tenantPortalWebService;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SupportAttachmentStorageService attachmentStorageService;
    private final SupportKnowledgeSuggestionService knowledgeSuggestionService;
    private final SupportDashboardUseCase supportDashboardUseCase;
    private final PlatformSettingService platformSettingService;

    @GetMapping
    public String index(
            Authentication authentication,
            @RequestParam(name = "assunto", required = false) String assunto,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "status", required = false) SupportTicketStatus status,
            @RequestParam(name = "prioridade", required = false) SupportTicketPriority prioridade,
            @RequestParam(name = "sla", required = false) SupportSlaState slaState,
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "responsavelId", required = false) Long responsavelId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Model model
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");

        model.addAttribute("view", supportTicketUseCase.list(
                user.getTenantId(),
                assunto,
                categoria,
                status,
                prioridade,
                slaState,
                clienteId,
                responsavelId,
                page,
                size
        ));
        model.addAttribute("statusValues", SupportTicketStatus.values());
        model.addAttribute("prioridadeValues", SupportTicketPriority.values());
        model.addAttribute("slaValues", SupportSlaState.values());
        model.addAttribute("clientes", listClientes(user.getTenantId()));
        model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
        model.addAttribute("canWrite", module.canWrite());
        populateCommon(authentication, model, "suporte", "Central de Suporte", "Gestão de chamados e SLA");
        return "tenant/support/index";
    }

    @GetMapping("/new")
    public String newTicket(Authentication authentication, Model model) {
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        if (!module.canWrite()) {
            return "redirect:/app/suporte";
        }

        model.addAttribute("form", new SupportTicketForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("statusValues", SupportTicketStatus.values());
        model.addAttribute("prioridadeValues", SupportTicketPriority.values());
        model.addAttribute("clientes", listClientes(user.getTenantId()));
        model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
        model.addAttribute("suggestions", List.of());
        populateCommon(authentication, model, "suporte", "Novo chamado", "Registrar solicitação de suporte");
        return "tenant/support/form";
    }

    @PostMapping
    public String create(
            Authentication authentication,
            @Valid @ModelAttribute("form") SupportTicketForm form,
            BindingResult bindingResult,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");

        if (bindingResult.hasErrors()) {
            populateFormModel(authentication, model, user.getTenantId(), false);
            model.addAttribute("suggestions", knowledgeSuggestionService.suggest(user.getTenantId(), form.getCategoria(), form.getAssunto()));
            return "tenant/support/form";
        }

        try {
            SupportTicketViewModel created = supportTicketUseCase.create(
                    user.getTenantId(),
                    user.getUsuarioId(),
                    user.getUsername(),
                    form,
                    files
            );
            redirectAttributes.addFlashAttribute("toastSuccess", "Chamado criado com sucesso.");
            addSmtpWarningToastIfNeeded(redirectAttributes);
            return "redirect:/app/suporte/" + created.id();
        } catch (AppException ex) {
            bindingResult.reject("support.create", ex.getMessage());
            populateFormModel(authentication, model, user.getTenantId(), false);
            model.addAttribute("suggestions", knowledgeSuggestionService.suggest(user.getTenantId(), form.getCategoria(), form.getAssunto()));
            return "tenant/support/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(Authentication authentication, @PathVariable("id") Long id, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");

        SupportTicketViewModel ticket = supportTicketUseCase.getById(user.getTenantId(), id);
        model.addAttribute("ticket", ticket);
        model.addAttribute("messageForm", new SupportTicketMessageForm());
        model.addAttribute("statusValues", SupportTicketStatus.values());
        model.addAttribute("canWrite", module.canWrite());
        model.addAttribute("canFull", module.canFull());
        model.addAttribute("suggestions", knowledgeSuggestionService.suggest(user.getTenantId(), ticket.categoria(), ticket.assunto()));
        populateCommon(authentication, model, "suporte", ticket.numero() + " - " + ticket.assunto(), "Detalhes do chamado");
        return "tenant/support/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(Authentication authentication, @PathVariable("id") Long id, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");
        if (!module.canWrite()) {
            return "redirect:/app/suporte/" + id;
        }

        SupportTicketViewModel ticket = supportTicketUseCase.getById(user.getTenantId(), id);
        model.addAttribute("form", toForm(ticket));
        model.addAttribute("ticket", ticket);
        model.addAttribute("isEdit", true);
        model.addAttribute("statusValues", SupportTicketStatus.values());
        model.addAttribute("prioridadeValues", SupportTicketPriority.values());
        model.addAttribute("clientes", listClientes(user.getTenantId()));
        model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(user.getTenantId()));
        model.addAttribute("suggestions", knowledgeSuggestionService.suggest(user.getTenantId(), ticket.categoria(), ticket.assunto()));
        populateCommon(authentication, model, "suporte", "Editar " + ticket.numero(), "Atualizar dados do chamado");
        return "tenant/support/form";
    }

    @PostMapping("/{id}")
    public String update(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("form") SupportTicketForm form,
            BindingResult bindingResult,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");
        if (bindingResult.hasErrors()) {
            SupportTicketViewModel ticket = supportTicketUseCase.getById(user.getTenantId(), id);
            model.addAttribute("ticket", ticket);
            populateFormModel(authentication, model, user.getTenantId(), true);
            model.addAttribute("suggestions", knowledgeSuggestionService.suggest(user.getTenantId(), form.getCategoria(), form.getAssunto()));
            return "tenant/support/form";
        }

        try {
            supportTicketUseCase.update(user.getTenantId(), id, user.getUsername(), form, files);
            redirectAttributes.addFlashAttribute("toastSuccess", "Chamado atualizado com sucesso.");
            return "redirect:/app/suporte/" + id;
        } catch (AppException ex) {
            bindingResult.reject("support.update", ex.getMessage());
            SupportTicketViewModel ticket = supportTicketUseCase.getById(user.getTenantId(), id);
            model.addAttribute("ticket", ticket);
            populateFormModel(authentication, model, user.getTenantId(), true);
            model.addAttribute("suggestions", knowledgeSuggestionService.suggest(user.getTenantId(), form.getCategoria(), form.getAssunto()));
            return "tenant/support/form";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestParam("status") SupportTicketStatus status,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");

        try {
            supportTicketUseCase.updateStatus(user.getTenantId(), id, status, user.getUsername());
            redirectAttributes.addFlashAttribute("toastSuccess", "Status atualizado.");
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/suporte/" + id;
    }

    @PostMapping("/{id}/messages")
    public String addMessage(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("messageForm") SupportTicketMessageForm form,
            BindingResult bindingResult,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");
        if (!module.canWrite()) {
            return "redirect:/app/suporte/" + id;
        }

        if (bindingResult.hasErrors()) {
            SupportTicketViewModel ticket = supportTicketUseCase.getById(user.getTenantId(), id);
            model.addAttribute("ticket", ticket);
            model.addAttribute("statusValues", SupportTicketStatus.values());
            model.addAttribute("canWrite", module.canWrite());
            model.addAttribute("canFull", module.canFull());
            model.addAttribute("suggestions", knowledgeSuggestionService.suggest(user.getTenantId(), ticket.categoria(), ticket.assunto()));
            populateCommon(authentication, model, "suporte", ticket.numero() + " - " + ticket.assunto(), "Detalhes do chamado");
            return "tenant/support/detail";
        }

        try {
            supportTicketUseCase.addMessage(user.getTenantId(), id, user.getUsuarioId(), user.getUsername(), form, files);
            redirectAttributes.addFlashAttribute("toastSuccess", "Comentário adicionado.");
            addSmtpWarningToastIfNeeded(redirectAttributes);
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/app/suporte/" + id;
    }

    @GetMapping("/settings")
    public String settings(Authentication authentication, Model model) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantPortalModuleViewModel module = tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");
        if (!module.canWrite()) {
            return "redirect:/app/suporte";
        }
        model.addAttribute("form", supportTicketUseCase.getSlaPolicy(user.getTenantId()));
        populateCommon(authentication, model, "suporte", "Configurações de SLA", "Parâmetros do atendimento");
        return "tenant/support/settings";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            @RequestParam(name = "periodDays", defaultValue = "30") int periodDays,
            Model model
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");
        model.addAttribute("dashboard", supportDashboardUseCase.summarize(user.getTenantId(), periodDays));
        model.addAttribute("periodDays", periodDays);
        populateCommon(authentication, model, "suporte", "Dashboard de Suporte", "Métricas operacionais e SLA");
        return "tenant/support/dashboard";
    }

    @PostMapping("/settings")
    public String updateSettings(
            Authentication authentication,
            @Valid @ModelAttribute("form") SupportSlaPolicyForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");

        if (bindingResult.hasErrors()) {
            populateCommon(authentication, model, "suporte", "Configurações de SLA", "Parâmetros do atendimento");
            return "tenant/support/settings";
        }

        try {
            supportTicketUseCase.updateSlaPolicy(user.getTenantId(), user.getUsername(), form);
            redirectAttributes.addFlashAttribute("toastSuccess", "SLA atualizado com sucesso.");
            return "redirect:/app/suporte/settings";
        } catch (AppException ex) {
            bindingResult.reject("support.sla.update", ex.getMessage());
            populateCommon(authentication, model, "suporte", "Configurações de SLA", "Parâmetros do atendimento");
            return "tenant/support/settings";
        }
    }

    @GetMapping("/anexos/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            Authentication authentication,
            @PathVariable("attachmentId") Long attachmentId
    ) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        tenantPortalWebService.requireEnabledModule(authentication, "SUPORTE");
        var attachment = supportTicketUseCase.findAttachmentOwnedByTenant(user.getTenantId(), attachmentId);
        Resource resource = attachmentStorageService.loadAsResource(attachment.getStoragePath());

        String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType(
                        attachment.getContentType() != null ? attachment.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
                ))
                .body(resource);
    }

    private SupportTicketForm toForm(SupportTicketViewModel ticket) {
        SupportTicketForm form = new SupportTicketForm();
        form.setAssunto(ticket.assunto());
        form.setDescricao(ticket.descricao());
        form.setCategoria(ticket.categoria());
        form.setStatus(ticket.status());
        form.setPrioridade(ticket.prioridade());
        form.setClienteId(ticket.clienteId());
        form.setResponsavelId(ticket.responsavelId());
        form.setSolicitanteNome(ticket.solicitanteNome());
        form.setSolicitanteEmail(ticket.solicitanteEmail());
        return form;
    }

    private List<Cliente> listClientes(Long tenantId) {
        return clienteRepository.findAll(ClienteSpecifications.byTenant(tenantId))
                .stream()
                .limit(30)
                .toList();
    }

    private void populateFormModel(Authentication authentication, Model model, Long tenantId, boolean isEdit) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("statusValues", SupportTicketStatus.values());
        model.addAttribute("prioridadeValues", SupportTicketPriority.values());
        model.addAttribute("clientes", listClientes(tenantId));
        model.addAttribute("responsaveis", usuarioRepository.findTop5ByTenantIdOrderByIdDesc(tenantId));
        populateCommon(authentication, model, "suporte", isEdit ? "Editar chamado" : "Novo chamado",
                isEdit ? "Atualizar dados do chamado" : "Registrar solicitação de suporte");
    }

    private void populateCommon(Authentication auth, Model model, String activeMenu, String pageTitle, String pageSubtitle) {
        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(auth));
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageSubtitle", pageSubtitle);
    }

    private void addSmtpWarningToastIfNeeded(RedirectAttributes redirectAttributes) {
        String smtpHost = platformSettingService.get("smtp.host", "");
        if (smtpHost == null || smtpHost.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "toastError",
                    "Notificação não enviada: SMTP não configurado. Configure em Admin > Configurações > Servidor de E-mail."
            );
        }
    }
}
