package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.system.HealthResponse;
import com.jaasielsilva.erpcorporativo.app.mapper.api.v1.system.SystemHealthApiMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetSystemHealthUseCase {

    private final SystemHealthApiMapper systemHealthApiMapper;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active:localhost}")
    private String activeProfile;

    public HealthResponse execute() {
        return systemHealthApiMapper.toResponse(applicationName, activeProfile);
    }
}
