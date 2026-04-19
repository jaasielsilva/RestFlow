package com.jaasielsilva.erpcorporativo.app.controller.web.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.erpcorporativo.app.service.web.auth.LoginWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginWebController {

    private final LoginWebService loginWebService;

    @GetMapping("/")
    public String root() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth.getPrincipal() instanceof String s && s.equals("anonymousUser"))) {
            // já logado — redireciona para a área correta
            return "redirect:/home";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginForm", loginWebService.createForm());
        return "auth/login";
    }
}
