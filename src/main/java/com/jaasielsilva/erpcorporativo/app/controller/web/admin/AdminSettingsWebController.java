package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsWebController {

    private final PlatformSettingService platformSettingService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("settings", platformSettingService.asMap());
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("pageTitle", "Configurações");
        model.addAttribute("pageSubtitle", "Parâmetros globais da plataforma");
        return "admin/settings/index";
    }

    @PostMapping
    public String save(
            @RequestParam Map<String, String> params,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        // Remove o token CSRF dos parâmetros antes de salvar
        params.remove("_csrf");
        platformSettingService.saveAll(params, authentication.getName());
        redirectAttributes.addFlashAttribute("toastSuccess", "Configurações salvas com sucesso.");
        return "redirect:/admin/settings";
    }
}
