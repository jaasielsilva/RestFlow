package com.jaasielsilva.erpcorporativo.app.service.api.v1.system;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.system.HealthResponse;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.system.GetSystemHealthUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemHealthApiService {

    private final GetSystemHealthUseCase getSystemHealthUseCase;

    public HealthResponse getHealth() {
        return getSystemHealthUseCase.execute();
    }
}
