package com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant;

import java.time.LocalDateTime;

public record TenantResponse(
        Long id,
        String nome,
        String slug,
        boolean ativo,
        long totalUsuarios,
        LocalDateTime createdAt
) {
}
