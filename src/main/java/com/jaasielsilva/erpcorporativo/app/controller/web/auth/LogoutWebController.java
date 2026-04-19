package com.jaasielsilva.erpcorporativo.app.controller.web.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LogoutWebController {

    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        if (authentication != null) {
            logoutHandler.logout(request, response, authentication);
        } else {
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();
        }

        return "redirect:/login";
    }
}
