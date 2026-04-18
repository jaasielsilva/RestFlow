package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginThrottleFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (isLoginAttempt(request)) {
            String clientKey = SecurityRequestUtils.extractClientKey(request);

            if (loginAttemptService.isBlocked(clientKey)) {
                response.sendRedirect("/login?blocked=true");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginAttempt(HttpServletRequest request) {
        return "/login".equals(request.getServletPath())
                && "POST".equalsIgnoreCase(request.getMethod());
    }
}
