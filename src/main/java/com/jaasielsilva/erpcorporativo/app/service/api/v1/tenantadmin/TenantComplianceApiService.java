package com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.ConsentLogRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.ConsentLogResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.LgpdRequestCreateRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.LgpdRequestResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.LgpdDataRequest;
import com.jaasielsilva.erpcorporativo.app.model.LgpdRequestStatus;
import com.jaasielsilva.erpcorporativo.app.model.TenantConsentLog;
import com.jaasielsilva.erpcorporativo.app.repository.compliance.LgpdDataRequestRepository;
import com.jaasielsilva.erpcorporativo.app.repository.compliance.TenantConsentLogRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class TenantComplianceApiService {

    private final LgpdDataRequestRepository lgpdDataRequestRepository;
    private final TenantConsentLogRepository tenantConsentLogRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public LgpdRequestResponse createRequest(Authentication authentication, LgpdRequestCreateRequest request) {
        var user = SecurityPrincipalUtils.getCurrentUser(authentication);
        var tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        var usuario = usuarioRepository.findById(user.getUsuarioId()).orElse(null);

        LgpdDataRequest entity = lgpdDataRequestRepository.save(LgpdDataRequest.builder()
                .tenant(tenant)
                .requestedBy(usuario)
                .requestType(request.requestType())
                .status(LgpdRequestStatus.ABERTA)
                .justificativa(request.justificativa())
                .build());
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<LgpdRequestResponse> listRequests(Authentication authentication) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        return lgpdDataRequestRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConsentLogResponse registerConsent(Authentication authentication, ConsentLogRequest request) {
        var user = SecurityPrincipalUtils.getCurrentUser(authentication);
        var tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        var usuario = usuarioRepository.findById(user.getUsuarioId()).orElse(null);

        TenantConsentLog log = tenantConsentLogRepository.save(TenantConsentLog.builder()
                .tenant(tenant)
                .usuario(usuario)
                .consentKey(request.consentKey())
                .accepted(request.accepted())
                .legalBasis(request.legalBasis())
                .build());
        return new ConsentLogResponse(log.getId(), log.getConsentKey(), log.isAccepted(), log.getLegalBasis(), log.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ConsentLogResponse> listConsents(Authentication authentication) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        return tenantConsentLogRepository.findTop100ByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(log -> new ConsentLogResponse(log.getId(), log.getConsentKey(), log.isAccepted(), log.getLegalBasis(), log.getCreatedAt()))
                .toList();
    }

    private LgpdRequestResponse toResponse(LgpdDataRequest entity) {
        return new LgpdRequestResponse(
                entity.getId(),
                entity.getRequestType(),
                entity.getStatus(),
                entity.getJustificativa(),
                entity.getResponseNote(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
