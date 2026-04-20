package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final PlatformSettingService platformSettingService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!requiresMaintenanceGate(uri) || !isMaintenanceEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isSuperAdmin()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (uri.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Plataforma em manutenção.\"}");
            return;
        }

        response.sendRedirect("/maintenance");
    }

    private boolean isMaintenanceEnabled() {
        return "true".equalsIgnoreCase(platformSettingService.get(PlatformSettingService.MAINTENANCE_MODE, "false"));
    }

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            return false;
        }
        return userDetails.getRole() == Role.SUPER_ADMIN;
    }

    private boolean requiresMaintenanceGate(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.startsWith("/app/")
                || uri.equals("/app")
                || uri.startsWith("/api/v1/tenant-admin/");
    }
}
