package com.jaasielsilva.erpcorporativo.app.service.web.admin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminDashboardViewModel;
import com.jaasielsilva.erpcorporativo.app.usecase.web.admin.BuildAdminDashboardUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardWebService {

    private final BuildAdminDashboardUseCase buildAdminDashboardUseCase;

    public AdminDashboardViewModel buildDashboard(Authentication authentication) {
        return buildAdminDashboardUseCase.execute(authentication);
    }
}
