package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminNotificationFeedResponse;
import com.jaasielsilva.erpcorporativo.app.service.shared.AdminNotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationApiController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping("/recent")
    public ApiResponse<AdminNotificationFeedResponse> recent() {
        var items = adminNotificationService.latestForAdmin().stream()
                .map(com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminNotificationItemResponse::from)
                .toList();
        return ApiResponse.success(new AdminNotificationFeedResponse(
                adminNotificationService.unreadCountForAdmin(),
                items
        ));
    }

    @PostMapping("/mark-all-read")
    public ApiResponse<String> markAllRead() {
        int affected = adminNotificationService.markAllAsReadForAdmin();
        return ApiResponse.success("Notificações marcadas como lidas: " + affected);
    }
}
