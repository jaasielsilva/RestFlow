package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance;

import java.time.LocalDateTime;

import com.jaasielsilva.erpcorporativo.app.model.LgpdRequestStatus;
import com.jaasielsilva.erpcorporativo.app.model.LgpdRequestType;

public record LgpdRequestResponse(
        Long id,
        LgpdRequestType requestType,
        LgpdRequestStatus status,
        String justificativa,
        String responseNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
