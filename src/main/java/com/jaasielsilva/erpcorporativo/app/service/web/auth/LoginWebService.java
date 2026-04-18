package com.jaasielsilva.erpcorporativo.app.service.web.auth;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.auth.LoginForm;
import com.jaasielsilva.erpcorporativo.app.usecase.web.auth.BuildLoginFormUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginWebService {

    private final BuildLoginFormUseCase buildLoginFormUseCase;

    public LoginForm createForm() {
        return buildLoginFormUseCase.execute();
    }
}
