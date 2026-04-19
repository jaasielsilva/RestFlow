package com.jaasielsilva.erpcorporativo.app.dto.api.admin.module;

import java.time.LocalDateTime;

public record PlatformModuleResponse(
        Long id,
        String codigo,
        String nome,
        String descricao,
        boolean ativo,
        LocalDateTime createdAt
) {
}
