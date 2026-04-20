package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.compliance.LgpdRequestUpdateRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.compliance.LgpdRequestResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.LgpdAdminApiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/compliance/lgpd")
@RequiredArgsConstructor
public class LgpdAdminApiController {

    private final LgpdAdminApiService lgpdAdminApiService;

    @GetMapping("/requests/open")
    public ApiResponse<List<LgpdRequestResponse>> openRequests() {
        return ApiResponse.success(lgpdAdminApiService.listOpen());
    }

    @PutMapping("/requests/{id}")
    public ApiResponse<LgpdRequestResponse> updateRequest(
            @PathVariable("id") Long id,
            @Valid @RequestBody LgpdRequestUpdateRequest request
    ) {
        return ApiResponse.success(lgpdAdminApiService.updateStatus(id, request));
    }
}
