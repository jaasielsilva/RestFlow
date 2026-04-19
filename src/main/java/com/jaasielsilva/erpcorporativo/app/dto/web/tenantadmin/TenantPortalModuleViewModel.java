package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

public record TenantPortalModuleViewModel(
        String codigo,
        String nome,
        String path,
        String iconClass,
        String toneClass,
        String activeKey
) {
}
