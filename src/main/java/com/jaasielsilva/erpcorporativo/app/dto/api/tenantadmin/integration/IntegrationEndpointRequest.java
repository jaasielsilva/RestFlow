package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IntegrationEndpointRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,
        @NotBlank(message = "Tipo de evento é obrigatório")
        @Size(max = 80, message = "Tipo de evento deve ter no máximo 80 caracteres")
        String eventType,
        @NotBlank(message = "URL é obrigatória")
        @Size(max = 500, message = "URL deve ter no máximo 500 caracteres")
        String url,
        @Size(max = 180, message = "Secret deve ter no máximo 180 caracteres")
        String secretKey,
        boolean ativo
) {
}
