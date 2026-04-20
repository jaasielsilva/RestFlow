package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.jaasielsilva.erpcorporativo.app.model.IntegrationDeliveryLog;
import com.jaasielsilva.erpcorporativo.app.model.IntegrationEndpoint;
import com.jaasielsilva.erpcorporativo.app.repository.integration.IntegrationDeliveryLogRepository;
import com.jaasielsilva.erpcorporativo.app.repository.integration.IntegrationEndpointRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntegrationWebhookDispatcherService {

    private final IntegrationEndpointRepository endpointRepository;
    private final IntegrationDeliveryLogRepository deliveryLogRepository;
    private final TenantRepository tenantRepository;

    public void dispatch(Long tenantId, String eventType, Map<String, Object> payload) {
        List<IntegrationEndpoint> endpoints = endpointRepository
                .findAllByTenantIdAndAtivoTrueAndEventType(tenantId, eventType);
        if (endpoints.isEmpty()) {
            return;
        }
        var tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant == null) {
            return;
        }

        for (IntegrationEndpoint endpoint : endpoints) {
            int status = 0;
            String responseBody = "";
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = RestClient.create()
                        .post()
                        .uri(endpoint.getUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, "RestFlow-Webhook/1.0")
                        .header("X-RestFlow-Event", eventType)
                        .header("X-RestFlow-Secret", endpoint.getSecretKey() != null ? endpoint.getSecretKey() : "")
                        .body(payload)
                        .retrieve()
                        .body(Map.class);
                status = 200;
                responseBody = response != null ? response.toString() : "";
            } catch (Exception ex) {
                status = 500;
                responseBody = ex.getMessage();
            }

            deliveryLogRepository.save(IntegrationDeliveryLog.builder()
                    .tenant(tenant)
                    .endpoint(endpoint)
                    .eventType(eventType)
                    .status(status)
                    .responseBody(responseBody)
                    .payloadJson(payload.toString())
                    .build());
        }
    }
}
