package com.jaasielsilva.erpcorporativo.app.usecase.web.auth;

import org.springframework.stereotype.Component;

import com.jaasielsilva.erpcorporativo.app.dto.web.auth.LoginForm;

@Component
public class BuildLoginFormUseCase {

    public LoginForm execute() {
        return new LoginForm();
    }
}
