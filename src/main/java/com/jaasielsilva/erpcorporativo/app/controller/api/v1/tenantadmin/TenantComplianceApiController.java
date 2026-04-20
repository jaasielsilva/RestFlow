package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.ConsentLogRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.ConsentLogResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.LgpdRequestCreateRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.LgpdRequestResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantComplianceApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-admin/compliance")
@RequiredArgsConstructor
public class TenantComplianceApiController {

    private final TenantComplianceApiService tenantComplianceApiService;

    @PostMapping("/lgpd/requests")
    public ApiResponse<LgpdRequestResponse> create(
            Authentication authentication,
            @Valid @RequestBody LgpdRequestCreateRequest request
    ) {
        return ApiResponse.success(tenantComplianceApiService.createRequest(authentication, request));
    }

    @GetMapping("/lgpd/requests")
    public ApiResponse<List<LgpdRequestResponse>> list(Authentication authentication) {
        return ApiResponse.success(tenantComplianceApiService.listRequests(authentication));
    }

    @PostMapping("/consents")
    public ApiResponse<ConsentLogResponse> registerConsent(
            Authentication authentication,
            @Valid @RequestBody ConsentLogRequest request
    ) {
        return ApiResponse.success(tenantComplianceApiService.registerConsent(authentication, request));
    }

    @GetMapping("/consents")
    public ApiResponse<List<ConsentLogResponse>> listConsents(Authentication authentication) {
        return ApiResponse.success(tenantComplianceApiService.listConsents(authentication));
    }
}
