package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class PlatformModulesWebController {

    @GetMapping("/subscriptions")
    public String subscriptions() {
        return "admin/subscriptions/index";
    }

    @GetMapping("/payments")
    public String payments() {
        return "admin/payments/index";
    }

    @GetMapping("/reports")
    public String reports() {
        return "admin/reports/index";
    }

    @GetMapping("/settings")
    public String settings() {
        return "admin/settings/index";
    }
}
