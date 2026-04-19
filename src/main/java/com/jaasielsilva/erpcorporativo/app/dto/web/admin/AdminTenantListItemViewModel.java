package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.time.LocalDateTime;

public record AdminTenantListItemViewModel(
        Long id,
        String nome,
        String slug,
        boolean ativo,
        long totalUsuarios,
        LocalDateTime createdAt,
        String adminEmail,
        String contractStatus
) {
}
