package com.jaasielsilva.erpcorporativo.app.dto.web.home;

import java.time.LocalDateTime;

public record HomeRecentTenantViewModel(
        String nome,
        boolean ativo,
        LocalDateTime createdAt
) {
}
