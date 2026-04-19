package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPlaceholderWebController {

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("pageTitle", "Configurações");
        model.addAttribute("pageSubtitle", "Parâmetros globais e preferências da plataforma");
        model.addAttribute("moduleName", "Configurações");
        return "admin/placeholder/index";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Relatórios");
        model.addAttribute("pageSubtitle", "Consolidação e análise dos dados da plataforma");
        model.addAttribute("moduleName", "Relatórios");
        return "admin/placeholder/index";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("activeMenu", "home");
        model.addAttribute("pageTitle", "Notificações");
        model.addAttribute("pageSubtitle", "Central de notificações da plataforma");
        model.addAttribute("moduleName", "Notificações");
        return "admin/placeholder/index";
    }
}
