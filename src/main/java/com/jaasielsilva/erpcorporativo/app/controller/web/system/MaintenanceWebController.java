package com.jaasielsilva.erpcorporativo.app.controller.web.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MaintenanceWebController {

    @GetMapping("/maintenance")
    public String maintenancePage() {
        return "maintenance/index";
    }
}
