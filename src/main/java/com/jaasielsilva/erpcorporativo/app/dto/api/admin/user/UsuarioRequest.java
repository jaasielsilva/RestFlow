package com.jaasielsilva.erpcorporativo.app.dto.api.admin.user;

import com.jaasielsilva.erpcorporativo.app.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        String email,

        @Size(max = 255, message = "Senha deve ter no máximo 255 caracteres")
        String password,

        boolean ativo,

        @NotNull(message = "Role é obrigatória")
        Role role,

        Long tenantId
) {
}
