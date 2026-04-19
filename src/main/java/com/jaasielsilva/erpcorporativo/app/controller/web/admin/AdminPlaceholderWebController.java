package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPlaceholderWebController {

    @GetMapping("/permissions")
    public String permissions(Model model) {
        model.addAttribute("activeMenu", "permissions");
        model.addAttribute("pageTitle", "Permissões");
        model.addAttribute("pageSubtitle", "Controle de permissões e políticas de acesso");
        model.addAttribute("moduleName", "Permissões");
        return "admin/placeholder/index";
    }

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
