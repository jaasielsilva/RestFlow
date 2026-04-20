package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration;

import java.time.LocalDateTime;

public record IntegrationEndpointResponse(
        Long id,
        String nome,
        String eventType,
        String url,
        boolean ativo,
        LocalDateTime createdAt
) {
}
