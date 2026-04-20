package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance;

import java.time.LocalDateTime;

public record ConsentLogResponse(
        Long id,
        String consentKey,
        boolean accepted,
        String legalBasis,
        LocalDateTime createdAt
) {
}
