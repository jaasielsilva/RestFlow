package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminTenantUpdateForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @NotBlank(message = "Slug é obrigatório")
    @Size(max = 80, message = "Slug deve ter no máximo 80 caracteres")
    private String slug;

    private boolean ativo = true;
}
