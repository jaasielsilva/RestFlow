package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance;

import com.jaasielsilva.erpcorporativo.app.model.LgpdRequestType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LgpdRequestCreateRequest(
        @NotNull(message = "Tipo de solicitação é obrigatório")
        LgpdRequestType requestType,
        @Size(max = 2000, message = "Justificativa deve ter no máximo 2000 caracteres")
        String justificativa
) {
}
