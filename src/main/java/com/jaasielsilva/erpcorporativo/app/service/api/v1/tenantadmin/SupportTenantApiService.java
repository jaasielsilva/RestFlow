package com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.PageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportAttachmentResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportDashboardResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportMessageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportSlaPolicyRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportSlaPolicyResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketFilter;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketListItemResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketMessageRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketPageResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support.SupportTicketResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportAttachmentViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportMessageViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportSlaPolicyForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketMessageForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportTicketViewModel;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.usecase.web.support.SupportTicketUseCase;
import com.jaasielsilva.erpcorporativo.app.usecase.web.support.SupportDashboardUseCase;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class SupportTenantApiService {

    private final SupportTicketUseCase supportTicketUseCase;
    private final SupportDashboardUseCase supportDashboardUseCase;

    public SupportTicketPageResponse list(Authentication authentication, SupportTicketFilter filter, int page, int size) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        var list = supportTicketUseCase.list(
                user.getTenantId(),
                filter.assunto(),
                filter.categoria(),
                filter.status(),
                filter.prioridade(),
                filter.slaState(),
                filter.clienteId(),
                filter.responsavelId(),
                page,
                size
        );

        var content = list.items().stream()
                .map(item -> new SupportTicketListItemResponse(
                        item.id(),
                        item.numero(),
                        item.assunto(),
                        item.categoria(),
                        item.status(),
                        item.prioridade(),
                        item.slaState(),
                        item.clienteNome(),
                        item.responsavelNome(),
                        item.resolutionDueAt(),
                        item.updatedAt()
                ))
                .toList();

        return new SupportTicketPageResponse(
                new PageResponse<>(content, list.currentPage(), size, list.totalElements(), list.totalPages()),
                list.totalAbertos(),
                list.totalAtendimento(),
                list.totalResolvidos(),
                list.totalViolados()
        );
    }

    public SupportTicketResponse getById(Authentication authentication, Long id) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        return toResponse(supportTicketUseCase.getById(user.getTenantId(), id));
    }

    public SupportTicketResponse create(Authentication authentication, SupportTicketRequest request) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        return toResponse(supportTicketUseCase.create(
                user.getTenantId(),
                user.getUsuarioId(),
                user.getUsername(),
                toForm(request),
                null
        ));
    }

    public SupportTicketResponse update(Authentication authentication, Long id, SupportTicketRequest request) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        return toResponse(supportTicketUseCase.update(
                user.getTenantId(),
                id,
                user.getUsername(),
                toForm(request),
                null
        ));
    }

    public void updateStatus(Authentication authentication, Long id, com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus status) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        supportTicketUseCase.updateStatus(user.getTenantId(), id, status, user.getUsername());
    }

    public SupportTicketResponse addMessage(Authentication authentication, Long id, SupportTicketMessageRequest request) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        SupportTicketMessageForm form = new SupportTicketMessageForm();
        form.setConteudo(request.conteudo());
        form.setVisibilidade(request.visibilidade());
        return toResponse(supportTicketUseCase.addMessage(
                user.getTenantId(),
                id,
                user.getUsuarioId(),
                user.getUsername(),
                form,
                null
        ));
    }

    public SupportSlaPolicyResponse getSlaPolicy(Authentication authentication) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        SupportSlaPolicyForm form = supportTicketUseCase.getSlaPolicy(user.getTenantId());
        return new SupportSlaPolicyResponse(
                form.getFirstResponseMinutes(),
                form.getResolutionMinutes(),
                form.getWarningBeforeMinutes()
        );
    }

    public SupportSlaPolicyResponse updateSlaPolicy(Authentication authentication, SupportSlaPolicyRequest request) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        SupportSlaPolicyForm form = new SupportSlaPolicyForm();
        form.setFirstResponseMinutes(request.firstResponseMinutes());
        form.setResolutionMinutes(request.resolutionMinutes());
        form.setWarningBeforeMinutes(request.warningBeforeMinutes());
        supportTicketUseCase.updateSlaPolicy(user.getTenantId(), user.getUsername(), form);
        return getSlaPolicy(authentication);
    }

    public SupportDashboardResponse dashboard(Authentication authentication, int periodDays) {
        AppUserDetails user = SecurityPrincipalUtils.getCurrentUser(authentication);
        var dashboard = supportDashboardUseCase.summarize(user.getTenantId(), periodDays);
        return new SupportDashboardResponse(
                dashboard.totalBacklog(),
                dashboard.totalAbertos(),
                dashboard.totalResolvidosPeriodo(),
                dashboard.totalViolados(),
                dashboard.mediaHorasPrimeiraResposta(),
                dashboard.mediaHorasResolucao(),
                dashboard.percentualSlaCumprido()
        );
    }

    private SupportTicketForm toForm(SupportTicketRequest request) {
        SupportTicketForm form = new SupportTicketForm();
        form.setAssunto(request.assunto());
        form.setDescricao(request.descricao());
        form.setCategoria(request.categoria());
        form.setStatus(request.status());
        form.setPrioridade(request.prioridade());
        form.setClienteId(request.clienteId());
        form.setResponsavelId(request.responsavelId());
        form.setSolicitanteNome(request.solicitanteNome());
        form.setSolicitanteEmail(request.solicitanteEmail());
        return form;
    }

    private SupportTicketResponse toResponse(SupportTicketViewModel view) {
        return new SupportTicketResponse(
                view.id(),
                view.numero(),
                view.assunto(),
                view.descricao(),
                view.categoria(),
                view.status(),
                view.prioridade(),
                view.slaState(),
                view.clienteId(),
                view.clienteNome(),
                view.solicitanteNome(),
                view.solicitanteEmail(),
                view.responsavelId(),
                view.responsavelNome(),
                view.firstResponseDueAt(),
                view.resolutionDueAt(),
                view.firstRespondedAt(),
                view.resolvedAt(),
                view.closedAt(),
                view.createdAt(),
                view.updatedAt(),
                view.mensagens().stream().map(this::toMessageResponse).toList(),
                view.anexos().stream().map(this::toAttachmentResponse).toList()
        );
    }

    private SupportMessageResponse toMessageResponse(SupportMessageViewModel view) {
        return new SupportMessageResponse(
                view.id(),
                view.autorNome(),
                view.conteudo(),
                view.visibilidade(),
                view.createdAt(),
                view.anexos().stream().map(this::toAttachmentResponse).toList()
        );
    }

    private SupportAttachmentResponse toAttachmentResponse(SupportAttachmentViewModel view) {
        return new SupportAttachmentResponse(
                view.id(),
                view.fileName(),
                view.contentType(),
                view.sizeBytes(),
                view.createdAt()
        );
    }
}
