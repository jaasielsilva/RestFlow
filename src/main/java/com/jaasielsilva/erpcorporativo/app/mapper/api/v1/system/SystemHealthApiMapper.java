package com.jaasielsilva.erpcorporativo.app.mapper.api.v1.system;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.system.HealthResponse;

@Component
public class SystemHealthApiMapper {

    public HealthResponse toResponse(String applicationName, String activeProfile) {
        return new HealthResponse(
                applicationName,
                activeProfile,
                OffsetDateTime.now()
        );
    }
}
