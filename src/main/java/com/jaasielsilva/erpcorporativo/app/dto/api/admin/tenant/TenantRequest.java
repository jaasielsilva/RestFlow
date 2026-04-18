package com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TenantRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "Slug é obrigatório")
        @Size(max = 80, message = "Slug deve ter no máximo 80 caracteres")
        String slug,

        boolean ativo,

        @Valid
        @NotNull(message = "Os dados do admin inicial são obrigatórios")
        TenantAdminRequest admin
) {
}
