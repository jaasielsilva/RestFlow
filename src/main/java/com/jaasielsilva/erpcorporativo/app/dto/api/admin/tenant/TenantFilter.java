package com.jaasielsilva.erpcorporativo.app.dto.api.admin.tenant;

public record TenantFilter(
        String nome,
        String slug,
        Boolean ativo
) {
}
