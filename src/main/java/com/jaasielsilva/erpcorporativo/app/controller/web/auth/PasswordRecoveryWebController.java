package com.jaasielsilva.erpcorporativo.app.controller.web.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PasswordRecoveryWebController {

    @GetMapping("/recuperar-senha")
    public String recovery() {
        return "auth/password-recovery";
    }
}

