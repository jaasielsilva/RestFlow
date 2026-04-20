package com.jaasielsilva.erpcorporativo.app.dto.api.admin.compliance;

import com.jaasielsilva.erpcorporativo.app.model.LgpdRequestStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LgpdRequestUpdateRequest(
        @NotNull(message = "Status é obrigatório")
        LgpdRequestStatus status,
        @Size(max = 2000, message = "Resposta deve ter no máximo 2000 caracteres")
        String responseNote
) {
}
