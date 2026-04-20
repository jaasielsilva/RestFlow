package com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration.IntegrationDeliveryLogResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration.IntegrationEndpointRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration.IntegrationEndpointResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.IntegrationEndpoint;
import com.jaasielsilva.erpcorporativo.app.repository.integration.IntegrationDeliveryLogRepository;
import com.jaasielsilva.erpcorporativo.app.repository.integration.IntegrationEndpointRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.shared.IntegrationWebhookDispatcherService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class TenantIntegrationApiService {

    private final IntegrationEndpointRepository endpointRepository;
    private final IntegrationDeliveryLogRepository deliveryLogRepository;
    private final TenantRepository tenantRepository;
    private final IntegrationWebhookDispatcherService webhookDispatcherService;

    @Transactional(readOnly = true)
    public List<IntegrationEndpointResponse> list(Authentication authentication) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        return endpointRepository.findAllByTenantIdOrderByNomeAsc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IntegrationEndpointResponse create(Authentication authentication, IntegrationEndpointRequest request) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        IntegrationEndpoint endpoint = IntegrationEndpoint.builder()
                .tenant(tenant)
                .nome(request.nome())
                .eventType(request.eventType())
                .url(request.url())
                .secretKey(request.secretKey())
                .ativo(request.ativo())
                .build();
        return toResponse(endpointRepository.save(endpoint));
    }

    @Transactional
    public IntegrationEndpointResponse update(Authentication authentication, Long id, IntegrationEndpointRequest request) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        IntegrationEndpoint endpoint = endpointRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint não encontrado."));
        endpoint.setNome(request.nome());
        endpoint.setEventType(request.eventType());
        endpoint.setUrl(request.url());
        endpoint.setSecretKey(request.secretKey());
        endpoint.setAtivo(request.ativo());
        return toResponse(endpointRepository.save(endpoint));
    }

    @Transactional
    public void triggerTest(Authentication authentication, Long id) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        IntegrationEndpoint endpoint = endpointRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Endpoint não encontrado."));
        webhookDispatcherService.dispatch(tenantId, endpoint.getEventType(), Map.of(
                "type", "test.event",
                "source", "restflow",
                "endpointId", endpoint.getId(),
                "message", "Webhook de teste disparado pelo tenant."
        ));
    }

    @Transactional(readOnly = true)
    public List<IntegrationDeliveryLogResponse> logs(Authentication authentication) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        return deliveryLogRepository.findTop50ByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(log -> new IntegrationDeliveryLogResponse(
                        log.getId(),
                        log.getEndpoint() != null ? log.getEndpoint().getId() : null,
                        log.getEndpoint() != null ? log.getEndpoint().getNome() : null,
                        log.getEventType(),
                        log.getStatus(),
                        log.getResponseBody(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    private IntegrationEndpointResponse toResponse(IntegrationEndpoint endpoint) {
        return new IntegrationEndpointResponse(
                endpoint.getId(),
                endpoint.getNome(),
                endpoint.getEventType(),
                endpoint.getUrl(),
                endpoint.isAtivo(),
                endpoint.getCreatedAt()
        );
    }
}
