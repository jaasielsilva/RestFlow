package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantBillingProfileRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantBillingProfileResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantCommercialProfileResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantCommercialApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-admin/commercial")
@RequiredArgsConstructor
public class TenantCommercialApiController {

    private final TenantCommercialApiService tenantCommercialApiService;

    @GetMapping("/profile")
    public ApiResponse<TenantCommercialProfileResponse> profile(Authentication authentication) {
        return ApiResponse.success(tenantCommercialApiService.profile(authentication));
    }

    @PutMapping("/billing")
    public ApiResponse<TenantBillingProfileResponse> updateBilling(
            Authentication authentication,
            @Valid @RequestBody TenantBillingProfileRequest request
    ) {
        return ApiResponse.success(tenantCommercialApiService.updateBillingProfile(authentication, request));
    }
}
