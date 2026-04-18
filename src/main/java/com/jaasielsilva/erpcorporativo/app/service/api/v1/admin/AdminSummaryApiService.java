package com.jaasielsilva.erpcorporativo.app.service.api.v1.admin;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminSummaryResponse;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.GetAdminSummaryUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSummaryApiService {

    private final GetAdminSummaryUseCase getAdminSummaryUseCase;

    public AdminSummaryResponse getSummary() {
        return getAdminSummaryUseCase.execute();
    }
}
