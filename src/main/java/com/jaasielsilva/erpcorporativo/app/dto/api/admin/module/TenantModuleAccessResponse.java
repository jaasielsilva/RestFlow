package com.jaasielsilva.erpcorporativo.app.dto.api.admin.module;

public record TenantModuleAccessResponse(
        Long tenantId,
        String tenantNome,
        Long moduleId,
        String moduleCodigo,
        String moduleNome,
        String moduleDescricao,
        boolean moduleAtivo,
        boolean enabledForTenant
) {
}
