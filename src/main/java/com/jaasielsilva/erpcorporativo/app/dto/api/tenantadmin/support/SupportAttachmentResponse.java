package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

import java.time.LocalDateTime;

public record SupportAttachmentResponse(
        Long id,
        String fileName,
        String contentType,
        long sizeBytes,
        LocalDateTime createdAt
) {
}
