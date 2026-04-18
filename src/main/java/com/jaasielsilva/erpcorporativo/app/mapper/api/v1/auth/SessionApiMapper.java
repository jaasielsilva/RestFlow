package com.jaasielsilva.erpcorporativo.app.mapper.api.v1.auth;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.auth.SessionResponse;

@Component
public class SessionApiMapper {

    public SessionResponse toResponse(Authentication authentication, Long tenantId) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();

        return new SessionResponse(
                authentication.getName(),
                tenantId,
                roles
        );
    }
}
