package com.jaasielsilva.erpcorporativo.app.mapper.api.v1.admin;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminSummaryResponse;

@Component
public class AdminSummaryApiMapper {

    public AdminSummaryResponse toResponse(long totalUsuarios, long totalTenants) {
        return new AdminSummaryResponse(totalUsuarios, totalTenants);
    }
}
