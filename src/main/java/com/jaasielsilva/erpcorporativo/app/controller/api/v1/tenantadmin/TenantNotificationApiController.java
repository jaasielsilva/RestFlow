package com.jaasielsilva.erpcorporativo.app.controller.api.v1.tenantadmin;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminNotificationFeedResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.AdminNotificationItemResponse;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.shared.AdminNotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant/notifications")
@RequiredArgsConstructor
public class TenantNotificationApiController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping("/recent")
    public ApiResponse<AdminNotificationFeedResponse> recent(Authentication authentication) {
        var user = SecurityPrincipalUtils.getCurrentUser(authentication);
        var items = adminNotificationService.latestForTenant(user.getTenantId()).stream()
                .map(AdminNotificationItemResponse::from)
                .toList();
        return ApiResponse.success(new AdminNotificationFeedResponse(
                adminNotificationService.unreadCountForTenant(user.getTenantId()),
                items
        ));
    }

    @PostMapping("/mark-all-read")
    public ApiResponse<String> markAllRead(Authentication authentication) {
        var user = SecurityPrincipalUtils.getCurrentUser(authentication);
        int affected = adminNotificationService.markAllAsReadForTenant(user.getTenantId());
        return ApiResponse.success("Notificações marcadas como lidas: " + affected);
    }
}
