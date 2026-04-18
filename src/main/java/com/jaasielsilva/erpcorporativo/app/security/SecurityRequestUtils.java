package com.jaasielsilva.erpcorporativo.app.security;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class SecurityRequestUtils {

    private SecurityRequestUtils() {
    }

    public static String extractClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
