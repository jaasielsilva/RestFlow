package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration.IntegrationDeliveryLogResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration.IntegrationEndpointRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.integration.IntegrationEndpointResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantIntegrationApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-admin/integrations")
@RequiredArgsConstructor
public class TenantIntegrationApiController {

    private final TenantIntegrationApiService tenantIntegrationApiService;

    @GetMapping("/endpoints")
    public ApiResponse<List<IntegrationEndpointResponse>> listEndpoints(Authentication authentication) {
        return ApiResponse.success(tenantIntegrationApiService.list(authentication));
    }

    @PostMapping("/endpoints")
    public ApiResponse<IntegrationEndpointResponse> createEndpoint(
            Authentication authentication,
            @Valid @RequestBody IntegrationEndpointRequest request
    ) {
        return ApiResponse.success(tenantIntegrationApiService.create(authentication, request));
    }

    @PutMapping("/endpoints/{id}")
    public ApiResponse<IntegrationEndpointResponse> updateEndpoint(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody IntegrationEndpointRequest request
    ) {
        return ApiResponse.success(tenantIntegrationApiService.update(authentication, id, request));
    }

    @PostMapping("/endpoints/{id}/test")
    public ApiResponse<String> testEndpoint(Authentication authentication, @PathVariable("id") Long id) {
        tenantIntegrationApiService.triggerTest(authentication, id);
        return ApiResponse.success("Webhook de teste enviado.");
    }

    @GetMapping("/logs")
    public ApiResponse<List<IntegrationDeliveryLogResponse>> logs(Authentication authentication) {
        return ApiResponse.success(tenantIntegrationApiService.logs(authentication));
    }
}
