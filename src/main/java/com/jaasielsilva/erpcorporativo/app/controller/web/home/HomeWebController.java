package com.jaasielsilva.erpcorporativo.app.controller.web.home;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jaasielsilva.erpcorporativo.app.dto.web.home.HomeViewModel;
import com.jaasielsilva.erpcorporativo.app.service.web.home.HomeWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeWebController {

    private final HomeWebService homeWebService;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        HomeViewModel homeView = homeWebService.buildHomeView(authentication);
        model.addAttribute("email", homeView.email());
        return "home/index";
    }
}
