package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PublicEndpointRateLimitFilter extends OncePerRequestFilter {

    private final PublicEndpointRateLimitService publicEndpointRateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isPublicStartRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = SecurityRequestUtils.extractClientKey(request);
        if (publicEndpointRateLimitService.isBlocked(clientKey)) {
            handleRateLimited(request, response);
            return;
        }

        publicEndpointRateLimitService.registerAttempt(clientKey);
        filterChain.doFilter(request, response);
    }

    private boolean isPublicStartRequest(HttpServletRequest request) {
        return "/planos/assinar".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod());
    }

    private void handleRateLimited(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/")) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Muitas tentativas. Aguarde alguns minutos.\"}");
            return;
        }
        response.sendRedirect("/planos?blocked="
                + URLEncoder.encode("Muitas tentativas detectadas. Aguarde 15 minutos.", StandardCharsets.UTF_8));
    }
}
