package com.jaasielsilva.erpcorporativo.app.controller.web.home;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.service.web.home.HomeWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeWebController {

    private final HomeWebService homeWebService;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.getPrincipal() instanceof AppUserDetails userDetails
                && userDetails.getRole() == Role.SUPER_ADMIN) {
            return "redirect:/admin";
        }

        HomeViewModel homeView = homeWebService.buildHomeView(authentication);
        model.addAttribute("view", homeView);
        model.addAttribute("email", homeView.email());
        return "home/index";
    }
}
