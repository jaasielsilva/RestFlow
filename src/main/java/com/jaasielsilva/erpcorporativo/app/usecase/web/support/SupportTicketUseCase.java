package com.jaasielsilva.erpcorporativo.app.usecase.web.support;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportAttachmentViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportSlaPolicyForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportMessageViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketListItemViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketListViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketMessageForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketViewModel;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.Cliente;
import com.jaasielsilva.erpcorporativo.app.model.SupportAttachment;
import com.jaasielsilva.erpcorporativo.app.model.SupportMessage;
import com.jaasielsilva.erpcorporativo.app.model.SupportMessageVisibility;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaPolicy;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportAttachmentRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportMessageRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportSlaPolicyRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportTicketRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportTicketSpecifications;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.service.shared.AuditService;
import com.jaasielsilva.erpcorporativo.app.service.shared.SupportAttachmentStorageService;
import com.jaasielsilva.erpcorporativo.app.service.shared.SupportAttachmentStorageService.StoredFile;
import com.jaasielsilva.erpcorporativo.app.service.shared.SupportNotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SupportTicketUseCase {

    private static final int DEFAULT_FIRST_RESPONSE_MINUTES = 120;
    private static final int DEFAULT_RESOLUTION_MINUTES = 1440;
    private static final int DEFAULT_WARNING_BEFORE_MINUTES = 120;

    private final SupportTicketRepository supportTicketRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final SupportAttachmentRepository supportAttachmentRepository;
    private final SupportSlaPolicyRepository supportSlaPolicyRepository;
    private final TenantRepository tenantRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SupportAttachmentStorageService attachmentStorageService;
    private final SupportNotificationService notificationService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public SupportTicketListViewModel list(
            Long tenantId,
            String assunto,
            String categoria,
            SupportTicketStatus status,
            com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority prioridade,
            SupportSlaState slaState,
            Long clienteId,
            Long responsavelId,
            int page,
            int size
    ) {
        Specification<SupportTicket> spec = Specification.allOf(
                SupportTicketSpecifications.byTenant(tenantId),
                SupportTicketSpecifications.byAssunto(assunto),
                SupportTicketSpecifications.byCategoria(categoria),
                SupportTicketSpecifications.byStatus(status),
                SupportTicketSpecifications.byPrioridade(prioridade),
                SupportTicketSpecifications.bySlaState(slaState),
                SupportTicketSpecifications.byCliente(clienteId),
                SupportTicketSpecifications.byResponsavel(responsavelId)
        );

        Page<SupportTicket> result = supportTicketRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by("updatedAt").descending())
        );

        List<SupportTicketListItemViewModel> items = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return new SupportTicketListViewModel(
                items,
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements(),
                supportTicketRepository.countByTenantIdAndStatus(tenantId, SupportTicketStatus.ABERTO),
                supportTicketRepository.countByTenantIdAndStatus(tenantId, SupportTicketStatus.EM_ATENDIMENTO),
                supportTicketRepository.countByTenantIdAndStatus(tenantId, SupportTicketStatus.RESOLVIDO),
                supportTicketRepository.countByTenantIdAndSlaState(tenantId, SupportSlaState.VIOLADO)
        );
    }

    @Transactional(readOnly = true)
    public SupportTicketViewModel getById(Long tenantId, Long id) {
        SupportTicket ticket = findOwned(tenantId, id);
        List<SupportMessage> messages = supportMessageRepository.findAllByTicketIdOrderByCreatedAtAsc(id);
        List<SupportAttachment> attachments = supportAttachmentRepository.findAllByTicketIdOrderByCreatedAtAsc(id);
        return toView(ticket, messages, attachments);
    }

    @Transactional
    public SupportTicketViewModel create(Long tenantId, Long usuarioId, String usuarioNome, SupportTicketForm form, MultipartFile[] files) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));

        SupportSlaPolicy policy = findOrCreatePolicy(tenant);
        LocalDateTime now = LocalDateTime.now();

        SupportTicket ticket = SupportTicket.builder()
                .numero(generateNumero(tenantId))
                .assunto(form.getAssunto())
                .descricao(form.getDescricao())
                .categoria(normalizeBlank(form.getCategoria()))
                .status(form.getStatus())
                .prioridade(form.getPrioridade())
                .solicitanteNome(normalizeBlank(form.getSolicitanteNome()))
                .solicitanteEmail(normalizeBlank(form.getSolicitanteEmail()))
                .firstResponseDueAt(now.plusMinutes(policy.getFirstResponseMinutes()))
                .resolutionDueAt(now.plusMinutes(policy.getResolutionMinutes()))
                .slaState(SupportSlaState.DENTRO_PRAZO)
                .cliente(resolveCliente(tenantId, form.getClienteId()))
                .responsavel(resolveResponsavel(tenantId, form.getResponsavelId()))
                .tenant(tenant)
                .build();

        supportTicketRepository.save(ticket);
        saveAttachments(tenantId, ticket, null, files);
        updateSlaState(ticket, policy, now);

        supportTicketRepository.save(ticket);

        auditService.log(
                AuditAction.SUPORTE_CHAMADO_CRIADO,
                "Chamado " + ticket.getNumero() + " criado.",
                "SupportTicket",
                ticket.getId(),
                usuarioNome,
                tenant
        );
        notificationService.notifyTicketCreated(ticket);
        return getById(tenantId, ticket.getId());
    }

    @Transactional
    public SupportTicketViewModel update(Long tenantId, Long id, String usuarioNome, SupportTicketForm form, MultipartFile[] files) {
        SupportTicket ticket = findOwned(tenantId, id);
        SupportSlaPolicy policy = findOrCreatePolicy(ticket.getTenant());
        LocalDateTime now = LocalDateTime.now();

        ticket.setAssunto(form.getAssunto());
        ticket.setDescricao(form.getDescricao());
        ticket.setCategoria(normalizeBlank(form.getCategoria()));
        ticket.setPrioridade(form.getPrioridade());
        ticket.setStatus(form.getStatus());
        ticket.setSolicitanteNome(normalizeBlank(form.getSolicitanteNome()));
        ticket.setSolicitanteEmail(normalizeBlank(form.getSolicitanteEmail()));
        ticket.setCliente(resolveCliente(tenantId, form.getClienteId()));
        ticket.setResponsavel(resolveResponsavel(tenantId, form.getResponsavelId()));
        applyStatusTimestamps(ticket, form.getStatus(), now);
        updateSlaState(ticket, policy, now);

        supportTicketRepository.save(ticket);
        saveAttachments(tenantId, ticket, null, files);

        auditService.log(
                AuditAction.SUPORTE_CHAMADO_ATUALIZADO,
                "Chamado " + ticket.getNumero() + " atualizado.",
                "SupportTicket",
                ticket.getId(),
                usuarioNome,
                ticket.getTenant()
        );
        return getById(tenantId, ticket.getId());
    }

    @Transactional
    public void updateStatus(Long tenantId, Long id, SupportTicketStatus status, String usuarioNome) {
        SupportTicket ticket = findOwned(tenantId, id);
        applyStatusTimestamps(ticket, status, LocalDateTime.now());
        ticket.setStatus(status);
        supportTicketRepository.save(ticket);

        auditService.log(
                AuditAction.SUPORTE_CHAMADO_STATUS_ATUALIZADO,
                "Status do chamado " + ticket.getNumero() + " alterado para " + status + ".",
                "SupportTicket",
                ticket.getId(),
                usuarioNome,
                ticket.getTenant()
        );
    }

    @Transactional
    public SupportTicketViewModel addMessage(
            Long tenantId,
            Long ticketId,
            Long usuarioId,
            String usuarioNome,
            SupportTicketMessageForm form,
            MultipartFile[] files
    ) {
        SupportTicket ticket = findOwned(tenantId, ticketId);
        Usuario author = usuarioRepository.findById(usuarioId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        SupportMessage message = SupportMessage.builder()
                .ticket(ticket)
                .tenant(ticket.getTenant())
                .autor(author)
                .autorNome(usuarioNome)
                .conteudo(form.getConteudo())
                .visibilidade(form.getVisibilidade())
                .build();

        supportMessageRepository.save(message);
        ticket.setLastMessageAt(now);
        if (ticket.getFirstRespondedAt() == null && form.getVisibilidade() == SupportMessageVisibility.PUBLICO) {
            ticket.setFirstRespondedAt(now);
            if (ticket.getStatus() == SupportTicketStatus.ABERTO) {
                ticket.setStatus(SupportTicketStatus.EM_ATENDIMENTO);
            }
        }

        saveAttachments(tenantId, ticket, message, files);
        updateSlaState(ticket, findOrCreatePolicy(ticket.getTenant()), now);
        supportTicketRepository.save(ticket);

        auditService.log(
                AuditAction.SUPORTE_COMENTARIO_ADICIONADO,
                "Comentário adicionado no chamado " + ticket.getNumero() + ".",
                "SupportMessage",
                message.getId(),
                usuarioNome,
                ticket.getTenant()
        );
        notificationService.notifyMessageAdded(ticket, message);
        return getById(tenantId, ticketId);
    }

    @Transactional(readOnly = true)
    public SupportAttachment findAttachmentOwnedByTenant(Long tenantId, Long attachmentId) {
        return supportAttachmentRepository.findById(attachmentId)
                .filter(att -> att.getTenant() != null && att.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Anexo não encontrado."));
    }

    @Transactional(readOnly = true)
    public SupportSlaPolicyForm getSlaPolicy(Long tenantId) {
        SupportSlaPolicy policy = supportSlaPolicyRepository.findByTenantId(tenantId)
                .orElseGet(() -> SupportSlaPolicy.builder()
                        .firstResponseMinutes(DEFAULT_FIRST_RESPONSE_MINUTES)
                        .resolutionMinutes(DEFAULT_RESOLUTION_MINUTES)
                        .warningBeforeMinutes(DEFAULT_WARNING_BEFORE_MINUTES)
                        .ativo(true)
                        .build());

        SupportSlaPolicyForm form = new SupportSlaPolicyForm();
        form.setFirstResponseMinutes(policy.getFirstResponseMinutes());
        form.setResolutionMinutes(policy.getResolutionMinutes());
        form.setWarningBeforeMinutes(policy.getWarningBeforeMinutes());
        return form;
    }

    @Transactional
    public void updateSlaPolicy(Long tenantId, String usuarioNome, SupportSlaPolicyForm form) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        SupportSlaPolicy policy = findOrCreatePolicy(tenant);

        policy.setFirstResponseMinutes(form.getFirstResponseMinutes());
        policy.setResolutionMinutes(form.getResolutionMinutes());
        policy.setWarningBeforeMinutes(form.getWarningBeforeMinutes());
        supportSlaPolicyRepository.save(policy);

        auditService.log(
                AuditAction.SUPORTE_SLA_ATUALIZADO,
                "Política de SLA do módulo de suporte atualizada.",
                "SupportSlaPolicy",
                policy.getId(),
                usuarioNome,
                tenant
        );
    }

    @Transactional
    public int refreshSlaStatesForTenant(Long tenantId) {
        SupportSlaPolicy policy = supportSlaPolicyRepository.findByTenantId(tenantId).orElse(null);
        if (policy == null) {
            return 0;
        }
        List<SupportTicketStatus> activeStatuses = List.of(
                SupportTicketStatus.ABERTO,
                SupportTicketStatus.EM_ATENDIMENTO,
                SupportTicketStatus.AGUARDANDO_CLIENTE
        );
        List<SupportTicket> tickets = supportTicketRepository.findAllByTenantIdAndStatusIn(tenantId, activeStatuses);
        LocalDateTime now = LocalDateTime.now();
        int changed = 0;
        for (SupportTicket ticket : tickets) {
            SupportSlaState before = ticket.getSlaState();
            updateSlaState(ticket, policy, now);
            if (before != ticket.getSlaState()) {
                changed++;
            }
        }
        supportTicketRepository.saveAll(tickets);
        return changed;
    }

    private void saveAttachments(Long tenantId, SupportTicket ticket, SupportMessage message, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            StoredFile stored = attachmentStorageService.store(tenantId, ticket.getId(), file);
            SupportAttachment attachment = SupportAttachment.builder()
                    .ticket(ticket)
                    .message(message)
                    .tenant(ticket.getTenant())
                    .fileName(stored.originalName())
                    .storedName(stored.storedName())
                    .storagePath(stored.absolutePath())
                    .contentType(stored.contentType())
                    .sizeBytes(stored.size())
                    .build();
            supportAttachmentRepository.save(attachment);
        }
    }

    private SupportTicket findOwned(Long tenantId, Long id) {
        return supportTicketRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado."));
    }

    private String generateNumero(Long tenantId) {
        int next = supportTicketRepository.findMaxSequenceByTenantId(tenantId) + 1;
        return String.format(Locale.ROOT, "SUP-%05d", next);
    }

    private Cliente resolveCliente(Long tenantId, Long clienteId) {
        if (clienteId == null) {
            return null;
        }
        return clienteRepository.findById(clienteId)
                .filter(cliente -> cliente.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ValidationException("Cliente informado não pertence ao tenant."));
    }

    private Usuario resolveResponsavel(Long tenantId, Long responsavelId) {
        if (responsavelId == null) {
            return null;
        }
        return usuarioRepository.findById(responsavelId)
                .filter(usuario -> usuario.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ValidationException("Responsável informado não pertence ao tenant."));
    }

    private SupportSlaPolicy findOrCreatePolicy(Tenant tenant) {
        return supportSlaPolicyRepository.findByTenantId(tenant.getId())
                .orElseGet(() -> supportSlaPolicyRepository.save(SupportSlaPolicy.builder()
                        .tenant(tenant)
                        .firstResponseMinutes(DEFAULT_FIRST_RESPONSE_MINUTES)
                        .resolutionMinutes(DEFAULT_RESOLUTION_MINUTES)
                        .warningBeforeMinutes(DEFAULT_WARNING_BEFORE_MINUTES)
                        .ativo(true)
                        .build()));
    }

    private void applyStatusTimestamps(SupportTicket ticket, SupportTicketStatus status, LocalDateTime now) {
        if (status == SupportTicketStatus.RESOLVIDO && ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(now);
        }
        if (status == SupportTicketStatus.FECHADO) {
            if (ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(now);
            }
            if (ticket.getClosedAt() == null) {
                ticket.setClosedAt(now);
            }
        }
    }

    private void updateSlaState(SupportTicket ticket, SupportSlaPolicy policy, LocalDateTime now) {
        if (ticket.getStatus() == SupportTicketStatus.RESOLVIDO || ticket.getStatus() == SupportTicketStatus.FECHADO) {
            ticket.setSlaState(SupportSlaState.DENTRO_PRAZO);
            return;
        }

        LocalDateTime firstResponseDueAt = ticket.getFirstResponseDueAt();
        LocalDateTime resolutionDueAt = ticket.getResolutionDueAt();
        LocalDateTime firstResponseThreshold = firstResponseDueAt != null
                ? firstResponseDueAt.minusMinutes(policy.getWarningBeforeMinutes())
                : null;
        LocalDateTime resolutionThreshold = resolutionDueAt != null
                ? resolutionDueAt.minusMinutes(policy.getWarningBeforeMinutes())
                : null;

        boolean firstResponseBreached = ticket.getFirstRespondedAt() == null
                && firstResponseDueAt != null
                && now.isAfter(firstResponseDueAt);
        boolean resolutionBreached = resolutionDueAt != null && now.isAfter(resolutionDueAt);

        if (firstResponseBreached || resolutionBreached) {
            ticket.setSlaState(SupportSlaState.VIOLADO);
            return;
        }

        boolean firstResponseWarning = ticket.getFirstRespondedAt() == null
                && firstResponseThreshold != null
                && (now.isAfter(firstResponseThreshold) || now.isEqual(firstResponseThreshold));
        boolean resolutionWarning = resolutionThreshold != null
                && (now.isAfter(resolutionThreshold) || now.isEqual(resolutionThreshold));

        ticket.setSlaState((firstResponseWarning || resolutionWarning)
                ? SupportSlaState.PROXIMO_VENCIMENTO
                : SupportSlaState.DENTRO_PRAZO);
    }

    private SupportTicketListItemViewModel toListItem(SupportTicket ticket) {
        return new SupportTicketListItemViewModel(
                ticket.getId(),
                ticket.getNumero(),
                ticket.getAssunto(),
                ticket.getCategoria(),
                ticket.getStatus(),
                ticket.getPrioridade(),
                ticket.getSlaState(),
                ticket.getCliente() != null ? ticket.getCliente().getNome() : null,
                ticket.getResponsavel() != null ? ticket.getResponsavel().getNome() : null,
                ticket.getResolutionDueAt(),
                ticket.getUpdatedAt()
        );
    }

    private SupportTicketViewModel toView(SupportTicket ticket, List<SupportMessage> messages, List<SupportAttachment> attachments) {
        Map<Long, List<SupportAttachment>> attachmentsByMessage = attachments.stream()
                .filter(attachment -> attachment.getMessage() != null)
                .collect(Collectors.groupingBy(attachment -> attachment.getMessage().getId()));

        List<SupportMessageViewModel> messageViews = messages.stream()
                .map(message -> new SupportMessageViewModel(
                        message.getId(),
                        message.getAutorNome() != null ? message.getAutorNome() : "Sistema",
                        message.getConteudo(),
                        message.getVisibilidade(),
                        message.getCreatedAt(),
                        attachmentsByMessage.getOrDefault(message.getId(), List.of()).stream()
                                .map(this::toAttachmentView)
                                .toList()
                ))
                .toList();

        List<SupportAttachmentViewModel> ticketAttachments = attachments.stream()
                .filter(attachment -> attachment.getMessage() == null)
                .map(this::toAttachmentView)
                .toList();

        return new SupportTicketViewModel(
                ticket.getId(),
                ticket.getNumero(),
                ticket.getAssunto(),
                ticket.getDescricao(),
                ticket.getCategoria(),
                ticket.getStatus(),
                ticket.getPrioridade(),
                ticket.getSlaState(),
                ticket.getCliente() != null ? ticket.getCliente().getId() : null,
                ticket.getCliente() != null ? ticket.getCliente().getNome() : null,
                ticket.getSolicitanteNome(),
                ticket.getSolicitanteEmail(),
                ticket.getResponsavel() != null ? ticket.getResponsavel().getId() : null,
                ticket.getResponsavel() != null ? ticket.getResponsavel().getNome() : null,
                ticket.getFirstResponseDueAt(),
                ticket.getResolutionDueAt(),
                ticket.getFirstRespondedAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                messageViews,
                ticketAttachments
        );
    }

    private SupportAttachmentViewModel toAttachmentView(SupportAttachment attachment) {
        return new SupportAttachmentViewModel(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getCreatedAt()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
