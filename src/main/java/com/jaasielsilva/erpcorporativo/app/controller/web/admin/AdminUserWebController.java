package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jaasielsilva.erpcorporativo.app.exception.AppException;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.web.admin.AdminUserWebService;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserWebController {

    private final AdminUserWebService adminUserWebService;

    @GetMapping
    public String index(
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "ativo", required = false) Boolean ativo,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Model model
    ) {
        model.addAttribute("view", adminUserWebService.list(nome, email, tenantId, ativo, role, page, size));
        return "admin/users/index";
    }

    @PostMapping("/{id}/reset-password")
    public String resetUserPassword(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            var result = adminUserWebService.resetPassword(id);
            log.info("Reset de senha executado para usuario {} com senha temporaria {}", result.userEmail(), result.generatedPassword());
            redirectAttributes.addFlashAttribute(
                    "toastSuccess",
                    "Senha resetada para: " + result.generatedPassword()
            );
        } catch (AppException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
