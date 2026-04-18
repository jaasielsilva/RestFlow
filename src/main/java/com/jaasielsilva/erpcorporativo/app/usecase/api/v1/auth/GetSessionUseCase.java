package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.api.auth.SessionResponse;
import com.jaasielsilva.erpcorporativo.app.mapper.api.v1.auth.SessionApiMapper;
import com.jaasielsilva.erpcorporativo.app.tenant.TenantContext;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetSessionUseCase {

    private final SessionApiMapper sessionApiMapper;

    public SessionResponse execute(Authentication authentication) {
        return sessionApiMapper.toResponse(authentication, TenantContext.getTenantId());
    }
}
