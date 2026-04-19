package com.jaasielsilva.erpcorporativo.app.dto.api.admin.module;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlatformModuleRequest(
        @NotBlank(message = "Código é obrigatório")
        @Size(max = 60, message = "Código deve ter no máximo 60 caracteres")
        String codigo,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String descricao,

        boolean ativo
) {
}
