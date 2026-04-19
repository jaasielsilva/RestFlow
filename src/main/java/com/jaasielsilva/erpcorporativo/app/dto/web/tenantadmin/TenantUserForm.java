package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import com.jaasielsilva.erpcorporativo.app.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TenantUserForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @Size(max = 255, message = "Senha deve ter no máximo 255 caracteres")
    private String password;

    private boolean ativo = true;

    @NotNull(message = "Perfil é obrigatório")
    private Role role = Role.USER;
}
