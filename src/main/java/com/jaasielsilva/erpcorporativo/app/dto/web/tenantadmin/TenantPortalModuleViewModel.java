package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;

public record TenantPortalModuleViewModel(
        String codigo,
        String nome,
        String path,
        String iconClass,
        String toneClass,
        String activeKey,
        AccessLevel accessLevel
) {
    /** Conveniência — mantém compatibilidade com código existente sem accessLevel explícito */
    public TenantPortalModuleViewModel(String codigo, String nome, String path,
                                       String iconClass, String toneClass, String activeKey) {
        this(codigo, nome, path, iconClass, toneClass, activeKey, AccessLevel.FULL);
    }

    public boolean canRead()  { return accessLevel != null && accessLevel != AccessLevel.NONE; }
    public boolean canWrite() { return accessLevel == AccessLevel.WRITE || accessLevel == AccessLevel.FULL; }
    public boolean canFull()  { return accessLevel == AccessLevel.FULL; }
}
