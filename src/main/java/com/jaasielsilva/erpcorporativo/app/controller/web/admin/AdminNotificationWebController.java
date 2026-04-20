package com.jaasielsilva.erpcorporativo.app.controller.web.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jaasielsilva.erpcorporativo.app.dto.web.shared.NotificationItemViewModel;
import com.jaasielsilva.erpcorporativo.app.service.shared.AdminNotificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationWebController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping
    public String index(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "readStatus", required = false) String readStatus,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        var notifications = adminNotificationService.filterForAdmin(type, readStatus, dateFrom, dateTo).stream()
                .map(NotificationItemViewModel::from)
                .toList();

        model.addAttribute("activeMenu", "home");
        model.addAttribute("pageTitle", "Notificações");
        model.addAttribute("pageSubtitle", "Timeline de eventos operacionais da plataforma");
        model.addAttribute("items", notifications);
        model.addAttribute("unreadCount", adminNotificationService.unreadCountForAdmin());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedReadStatus", readStatus);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        return "admin/notifications/index";
    }
}
