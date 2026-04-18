package com.jaasielsilva.erpcorporativo.app.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.jaasielsilva.erpcorporativo.app.model.Role;

public class AppUserDetails extends User {

    private final Long usuarioId;
    private final Long tenantId;
    private final Role role;

    public AppUserDetails(
            Long usuarioId,
            Long tenantId,
            Role role,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, true, true, true, true, authorities);
        this.usuarioId = usuarioId;
        this.tenantId = tenantId;
        this.role = role;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Role getRole() {
        return role;
    }
}
