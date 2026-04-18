package com.jaasielsilva.erpcorporativo.app.service.api.v1.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.auth.SessionResponse;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.auth.GetSessionUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionApiService {

    private final GetSessionUseCase getSessionUseCase;

    public SessionResponse getSession(Authentication authentication) {
        return getSessionUseCase.execute(authentication);
    }
}
