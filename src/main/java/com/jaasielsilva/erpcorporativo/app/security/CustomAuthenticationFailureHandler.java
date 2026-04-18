package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String clientKey = SecurityRequestUtils.extractClientKey(request);
        loginAttemptService.loginFailed(clientKey);

        String redirectUrl = loginAttemptService.isBlocked(clientKey)
                ? "/login?blocked=true"
                : "/login?error=true";

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
