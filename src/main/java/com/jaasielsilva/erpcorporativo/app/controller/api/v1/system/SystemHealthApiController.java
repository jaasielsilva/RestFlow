package com.jaasielsilva.erpcorporativo.app.controller.api.v1.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.system.HealthResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.system.SystemHealthApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemHealthApiController {

    private final SystemHealthApiService systemHealthApiService;

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(systemHealthApiService.getHealth());
    }
}
