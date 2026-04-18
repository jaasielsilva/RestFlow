package com.jaasielsilva.erpcorporativo.app.security;

import org.springframework.security.core.Authentication;

import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;

public final class SecurityPrincipalUtils {

    private SecurityPrincipalUtils() {
    }

    public static AppUserDetails getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            throw new ValidationException("Usuário autenticado inválido.");
        }

        return userDetails;
    }
}
