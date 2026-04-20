package com.jaasielsilva.erpcorporativo.app.controller.web.tenantadmin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.dto.web.shared.NotificationItemViewModel;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.shared.AdminNotificationService;
import com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin.TenantPortalWebService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/app/notifications")
@RequiredArgsConstructor
public class TenantNotificationWebController {

    private final TenantPortalWebService tenantPortalWebService;
    private final AdminNotificationService adminNotificationService;

    @GetMapping
    public String index(
            Authentication authentication,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "readStatus", required = false) String readStatus,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        var user = SecurityPrincipalUtils.getCurrentUser(authentication);
        var notifications = adminNotificationService
                .filterForTenant(user.getTenantId(), type, readStatus, dateFrom, dateTo).stream()
                .map(NotificationItemViewModel::from)
                .toList();

        model.addAttribute("tenantModules", tenantPortalWebService.listEnabledModules(authentication));
        model.addAttribute("activeMenu", "notifications");
        model.addAttribute("pageTitle", "Notificações");
        model.addAttribute("pageSubtitle", "Atualizações financeiras e operacionais do seu tenant");
        model.addAttribute("items", notifications);
        model.addAttribute("unreadCount", adminNotificationService.unreadCountForTenant(user.getTenantId()));
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedReadStatus", readStatus);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        return "tenant/notifications/index";
    }
}
