package com.jaasielsilva.erpcorporativo.app.dto.web.support;

import java.time.LocalDateTime;

public record SupportAttachmentViewModel(
        Long id,
        String fileName,
        String contentType,
        long sizeBytes,
        LocalDateTime createdAt
) {
}
