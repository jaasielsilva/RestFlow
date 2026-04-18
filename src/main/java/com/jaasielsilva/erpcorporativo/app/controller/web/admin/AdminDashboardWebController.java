package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminDashboardViewModel;
import com.jaasielsilva.erpcorporativo.app.service.web.admin.AdminDashboardWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardWebController {

    private final AdminDashboardWebService adminDashboardWebService;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        AdminDashboardViewModel dashboard = adminDashboardWebService.buildDashboard(authentication);
        model.addAttribute("dashboard", dashboard);
        return "admin/index";
    }
}
