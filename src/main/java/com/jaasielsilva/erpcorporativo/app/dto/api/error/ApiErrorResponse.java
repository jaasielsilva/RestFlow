package com.jaasielsilva.erpcorporativo.app.dto.api.error;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        String status,
        String code,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp
) {
}
