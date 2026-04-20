package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.service.web.admin.AdminUserWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
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
}
