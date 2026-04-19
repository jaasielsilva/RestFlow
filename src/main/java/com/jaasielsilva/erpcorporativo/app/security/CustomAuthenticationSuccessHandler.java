package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
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

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        loginAttemptService.loginSucceeded(SecurityRequestUtils.extractClientKey(request));

        if (authentication.getPrincipal() instanceof AppUserDetails userDetails
                && userDetails.getRole() == Role.SUPER_ADMIN) {
            getRedirectStrategy().sendRedirect(request, response, "/home");
            return;
        }

        if (authentication.getPrincipal() instanceof AppUserDetails userDetails
                && userDetails.getRole() == Role.ADMIN) {
            getRedirectStrategy().sendRedirect(request, response, "/app");
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso restrito ao SUPER_ADMIN.");
    }
}
