package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminTenantCreateForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @NotBlank(message = "Slug é obrigatório")
    @Size(max = 80, message = "Slug deve ter no máximo 80 caracteres")
    private String slug;

    private boolean ativo = true;

    @NotBlank(message = "Nome do admin é obrigatório")
    @Size(max = 150, message = "Nome do admin deve ter no máximo 150 caracteres")
    private String adminNome;

    @NotBlank(message = "Email do admin é obrigatório")
    @Email(message = "Email do admin inválido")
    @Size(max = 150, message = "Email do admin deve ter no máximo 150 caracteres")
    private String adminEmail;

    @NotBlank(message = "Senha do admin é obrigatória")
    @Size(min = 6, max = 255, message = "Senha do admin deve ter entre 6 e 255 caracteres")
    private String adminPassword;
}
