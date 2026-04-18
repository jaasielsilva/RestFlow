package com.jaasielsilva.erpcorporativo.app.tenant;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantRequestFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String TENANT_PARAMETER = "tenantId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            resolveTenantId(request).ifPresent(TenantContext::setTenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private java.util.Optional<Long> resolveTenantId(HttpServletRequest request) {
        String tenantIdValue = request.getHeader(TENANT_HEADER);

        if (!StringUtils.hasText(tenantIdValue)) {
            tenantIdValue = request.getParameter(TENANT_PARAMETER);
        }

        if (!StringUtils.hasText(tenantIdValue)) {
            return java.util.Optional.empty();
        }

        try {
            return java.util.Optional.of(Long.parseLong(tenantIdValue));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }
}
