package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminSummaryResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.AdminSummaryApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSummaryApiController {

    private final AdminSummaryApiService adminSummaryApiService;

    @GetMapping("/summary")
    public ApiResponse<AdminSummaryResponse> summary() {
        return ApiResponse.success(adminSummaryApiService.getSummary());
    }
}
