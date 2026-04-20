package com.jaasielsilva.erpcorporativo.app.tenant;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaasielsilva.erpcorporativo.app.dto.api.error.ApiErrorResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ApiErrorCode;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantRequestFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String TENANT_PARAMETER = "tenantId";

    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

    public TenantRequestFilter(TenantRepository tenantRepository, ObjectMapper objectMapper) {
        this.tenantRepository = tenantRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Bypass para rotas públicas — não precisam de processamento de tenant
        String uri = request.getRequestURI();
        if (isPublicRoute(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            boolean apiRequest = isApiRequest(request);
            boolean tenantRequired = requiresTenant(request);

            ResolvedTenant resolvedTenant = resolveTenant(request);

            if (resolvedTenant.isMalformed()) {
                writeApiError(
                        response,
                        HttpStatus.BAD_REQUEST,
                        ApiErrorCode.BAD_REQUEST,
                        "tenantId inválido.",
                        request.getRequestURI(),
                        apiRequest
                );
                return;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AppUserDetails currentUser = extractUser(authentication);

            if (currentUser != null && currentUser.getRole() != Role.SUPER_ADMIN) {
                Long principalTenantId = currentUser.getTenantId();

                if (principalTenantId == null) {
                    writeApiError(
                            response,
                            HttpStatus.UNAUTHORIZED,
                            ApiErrorCode.UNAUTHORIZED,
                            "Usuário autenticado sem tenant associado.",
                            request.getRequestURI(),
                            apiRequest
                    );
                    return;
                }

                Tenant tenant = tenantRepository.findById(principalTenantId)
                        .filter(Tenant::isAtivo)
                        .orElse(null);

                if (tenant == null) {
                    writeApiError(
                            response,
                            HttpStatus.UNAUTHORIZED,
                            ApiErrorCode.UNAUTHORIZED,
                            "Tenant inexistente ou inativo.",
                            request.getRequestURI(),
                            apiRequest
                    );
                    return;
                }

                if (resolvedTenant.isPresent() && !principalTenantId.equals(resolvedTenant.tenantId)) {
                    String principalTenantLabel = formatTenantLabel(tenant);
                    String requestedTenantLabel = tenantRepository.findById(resolvedTenant.tenantId)
                            .map(this::formatTenantLabel)
                            .orElse("tenantId=" + resolvedTenant.tenantId);
                    String friendlyMessage = "Voce esta logado no tenant " + principalTenantLabel
                            + ", mas tentou acessar o tenant " + requestedTenantLabel + ".";
                    if (!apiRequest) {
                        response.sendRedirect("/login?erro="
                                + URLEncoder.encode(friendlyMessage, StandardCharsets.UTF_8));
                        return;
                    }
                    writeApiError(
                            response,
                            HttpStatus.UNAUTHORIZED,
                            ApiErrorCode.UNAUTHORIZED,
                            friendlyMessage,
                            request.getRequestURI(),
                            apiRequest
                    );
                    return;
                }

                TenantContext.setTenantId(principalTenantId);
                filterChain.doFilter(request, response);
                return;
            }

            if (tenantRequired && resolvedTenant.isMissing()) {
                writeApiError(
                        response,
                        HttpStatus.BAD_REQUEST,
                        ApiErrorCode.BAD_REQUEST,
                        "Tenant obrigatório. Informe o tenantId via header X-Tenant-Id ou parâmetro tenantId.",
                        request.getRequestURI(),
                        apiRequest
                );
                return;
            }

            if (resolvedTenant.isPresent()) {
                Tenant tenant = tenantRepository.findById(resolvedTenant.tenantId)
                        .filter(Tenant::isAtivo)
                        .orElse(null);

                if (tenant == null) {
                    writeApiError(
                            response,
                            HttpStatus.UNAUTHORIZED,
                            ApiErrorCode.UNAUTHORIZED,
                            "Tenant inexistente ou inativo.",
                            request.getRequestURI(),
                            apiRequest
                    );
                    return;
                }

                TenantContext.setTenantId(resolvedTenant.tenantId);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private AppUserDetails extractUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            return null;
        }

        return userDetails;
    }

    private ResolvedTenant resolveTenant(HttpServletRequest request) {
        String tenantIdValue = request.getHeader(TENANT_HEADER);

        if (!StringUtils.hasText(tenantIdValue)) {
            tenantIdValue = request.getParameter(TENANT_PARAMETER);
        }

        if (!StringUtils.hasText(tenantIdValue)) {
            return ResolvedTenant.missing();
        }

        try {
            return ResolvedTenant.present(Long.parseLong(tenantIdValue));
        } catch (NumberFormatException ex) {
            return ResolvedTenant.malformed();
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/");
    }

    private boolean isPublicRoute(String uri) {
        if (uri == null) return false;
        return uri.equals("/")
                || uri.startsWith("/planos")
                || uri.startsWith("/assinatura")
                || uri.equals("/logout")
                || uri.equals("/error")
                || uri.startsWith("/recuperar-senha")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/img/")
                || uri.startsWith("/webjars/")
                || uri.equals("/favicon.ico")
                || uri.equals("/api/v1/system/health")
                || uri.startsWith("/api/v1/system/webhooks/mercadopago");
    }

    private boolean requiresTenant(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/v1/tenant-admin/");
    }

    private String formatTenantLabel(Tenant tenant) {
        return tenant.getNome() + " (id=" + tenant.getId() + ")";
    }

    private void writeApiError(
            HttpServletResponse response,
            HttpStatus status,
            ApiErrorCode code,
            String message,
            String path,
            boolean apiRequest
    ) throws IOException {
        response.setStatus(status.value());

        if (!apiRequest) {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write(message);
            return;
        }

        ApiErrorResponse body = new ApiErrorResponse(
                "error",
                code.name(),
                code.name().toLowerCase(),
                message,
                path,
                OffsetDateTime.now()
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private static final class ResolvedTenant {
        private final Long tenantId;
        private final boolean missing;
        private final boolean malformed;

        private ResolvedTenant(Long tenantId, boolean missing, boolean malformed) {
            this.tenantId = tenantId;
            this.missing = missing;
            this.malformed = malformed;
        }

        private static ResolvedTenant present(Long tenantId) {
            return new ResolvedTenant(tenantId, false, false);
        }

        private static ResolvedTenant missing() {
            return new ResolvedTenant(null, true, false);
        }

        private static ResolvedTenant malformed() {
            return new ResolvedTenant(null, false, true);
        }

        private boolean isPresent() {
            return tenantId != null;
        }

        private boolean isMissing() {
            return missing;
        }

        private boolean isMalformed() {
            return malformed;
        }
    }
}
