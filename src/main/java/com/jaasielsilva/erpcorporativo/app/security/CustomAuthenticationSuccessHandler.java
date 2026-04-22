package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.model.Role;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;
    private final com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService platformSettingService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        loginAttemptService.loginSucceeded(SecurityRequestUtils.extractClientKey(request));

        // Garante persistência explícita do contexto de segurança na sessão HTTP.
        SecurityContext context = SecurityContextHolder.getContext();
        securityContextRepository.saveContext(context, request, response);

        // Aplica o timeout configurado globalmente no banco via painel admin
        try {
            int timeoutMinutes = Integer.parseInt(platformSettingService.get(
                com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService.SESSION_TIMEOUT, "60"
            ));
            request.getSession().setMaxInactiveInterval(timeoutMinutes * 60);
        } catch (NumberFormatException e) {
            request.getSession().setMaxInactiveInterval(60 * 60);
        }

        if (authentication.getPrincipal() instanceof AppUserDetails userDetails
                && userDetails.getRole() == Role.SUPER_ADMIN) {
            getRedirectStrategy().sendRedirect(request, response, "/admin");
            return;
        }

        if (authentication.getPrincipal() instanceof AppUserDetails userDetails
                && userDetails.getRole() == Role.ADMIN) {
            getRedirectStrategy().sendRedirect(request, response, "/app");
            return;
        }

        // USER também vai para o portal do tenant
        if (authentication.getPrincipal() instanceof AppUserDetails) {
            getRedirectStrategy().sendRedirect(request, response, "/app");
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso não autorizado.");
    }
}
