package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsentLogRequest(
        @NotBlank(message = "Chave de consentimento é obrigatória")
        @Size(max = 80, message = "Chave deve ter no máximo 80 caracteres")
        String consentKey,
        boolean accepted,
        @Size(max = 120, message = "Base legal deve ter no máximo 120 caracteres")
        String legalBasis
) {
}
