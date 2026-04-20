package com.jaasielsilva.erpcorporativo.app.controller.web.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.erpcorporativo.app.service.web.auth.LoginWebService;

import jakarta.servlet.http.HttpServletRequest;
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
        return "redirect:/planos";
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        // A view de login é extensa e pode começar a enviar chunks antes do <form>.
        // Forçamos a sessão aqui para evitar falha tardia na criação do token CSRF.
        request.getSession(true);
        model.addAttribute("loginForm", loginWebService.createForm());
        return "auth/login";
    }
}
