package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminModuleCreateForm {

    @NotBlank(message = "Código é obrigatório")
    @Size(max = 60, message = "Código deve ter no máximo 60 caracteres")
    private String codigo;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @Size(max = 120, message = "Rota deve ter no máximo 120 caracteres")
    private String rota;

    private boolean ativo = true;
}
