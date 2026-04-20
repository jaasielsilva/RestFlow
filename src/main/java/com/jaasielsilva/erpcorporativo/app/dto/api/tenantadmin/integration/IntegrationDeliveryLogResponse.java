package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration;

import java.time.LocalDateTime;

public record IntegrationDeliveryLogResponse(
        Long id,
        Long endpointId,
        String endpointNome,
        String eventType,
        int status,
        String responseBody,
        LocalDateTime createdAt
) {
}
