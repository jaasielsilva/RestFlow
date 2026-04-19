package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPlaceholderWebController {

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("activeMenu", "home");
        model.addAttribute("pageTitle", "Notificações");
        model.addAttribute("pageSubtitle", "Central de notificações da plataforma");
        model.addAttribute("moduleName", "Notificações");
        return "admin/placeholder/index";
    }
}
