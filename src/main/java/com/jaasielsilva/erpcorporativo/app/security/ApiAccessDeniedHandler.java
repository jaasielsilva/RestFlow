package com.jaasielsilva.erpcorporativo.app.security;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.erpcorporativo.app.dto.api.error.ApiErrorResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ApiErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        String uri = request.getRequestURI();
        boolean isApiRequest = uri != null && uri.startsWith("/api/");

        if (!isApiRequest) {
            // Rota web — redireciona para login
            response.sendRedirect("/login");
            return;
        }

        ApiErrorResponse body = new ApiErrorResponse(
                "error",
                ApiErrorCode.ACCESS_DENIED.name(),
                "access_denied",
                "Você não possui permissão para acessar este recurso.",
                request.getRequestURI(),
                OffsetDateTime.now()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
