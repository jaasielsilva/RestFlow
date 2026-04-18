package com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantAdminRequest(
        @NotBlank(message = "Nome do admin é obrigatório")
        @Size(max = 150, message = "Nome do admin deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "Email do admin é obrigatório")
        @Email(message = "Email do admin inválido")
        @Size(max = 150, message = "Email do admin deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "Senha do admin é obrigatória")
        @Size(min = 6, max = 255, message = "Senha do admin deve ter entre 6 e 255 caracteres")
        String password
) {
}
