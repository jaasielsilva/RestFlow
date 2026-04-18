package com.jaasielsilva.erpcorporativo.app.controller.web.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.erpcorporativo.app.service.web.auth.LoginWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginWebController {

    private final LoginWebService loginWebService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginForm", loginWebService.createForm());
        return "auth/login";
    }
}
