package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        String tenantId = request.getParameter("tenantId");
        String tenantQuery = StringUtils.hasText(tenantId)
                ? "&tenantId=" + URLEncoder.encode(tenantId, StandardCharsets.UTF_8)
                : "";

        String redirectUrl;
        if (loginAttemptService.isBlocked(clientKey)) {
            redirectUrl = "/login?blocked=true" + tenantQuery;
        } else if (exception.getMessage() != null
                && exception.getMessage().contains("múltiplos tenants")) {
            redirectUrl = "/login?erro="
                    + URLEncoder.encode("Informe o tenantId para autenticar este e-mail.", StandardCharsets.UTF_8)
                    + tenantQuery;
        } else {
            redirectUrl = "/login?error=true" + tenantQuery;
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
