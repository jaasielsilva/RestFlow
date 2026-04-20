package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.bi.TenantAdvancedBiResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin.TenantAdvancedBiApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-admin/bi")
@RequiredArgsConstructor
public class TenantAdvancedBiApiController {

    private final TenantAdvancedBiApiService tenantAdvancedBiApiService;

    @GetMapping("/advanced")
    public ApiResponse<TenantAdvancedBiResponse> advanced(Authentication authentication) {
        return ApiResponse.success(tenantAdvancedBiApiService.summary(authentication));
    }
}
