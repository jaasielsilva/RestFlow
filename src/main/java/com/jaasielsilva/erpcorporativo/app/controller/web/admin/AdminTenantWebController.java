package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.service.web.admin.AdminTenantWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping({"/admin/tenants", "/admin/restaurants"})
@RequiredArgsConstructor
public class AdminTenantWebController {

    private final AdminTenantWebService adminTenantWebService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        model.addAttribute("view", adminTenantWebService.list(nome, slug, ativo, page, size));
        return "admin/tenants/index";
    }
}
