package com.jaasielsilva.erpcorporativo.app.controller.api.v1.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.auth.SessionResponse;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.auth.SessionApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SessionApiController {

    private final SessionApiService sessionApiService;

    @GetMapping("/session")
    public ApiResponse<SessionResponse> session(Authentication authentication) {
        return ApiResponse.success(sessionApiService.getSession(authentication));
    }
}
