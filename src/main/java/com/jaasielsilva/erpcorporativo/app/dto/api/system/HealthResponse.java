package com.jaasielsilva.erpcorporativo.app.dto.api.system;

import java.time.OffsetDateTime;

public record HealthResponse(
        String application,
        String environment,
        OffsetDateTime timestamp
) {
}
