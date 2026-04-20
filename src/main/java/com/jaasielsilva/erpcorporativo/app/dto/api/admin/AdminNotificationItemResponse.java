package com.jaasielsilva.erpcorporativo.app.dto.api.admin;

import java.time.LocalDateTime;

import com.jaasielsilva.erpcorporativo.app.model.PlatformNotification;

public record AdminNotificationItemResponse(
        Long id,
        String title,
        String message,
        String type,
        boolean read,
        LocalDateTime createdAt
) {
    public static AdminNotificationItemResponse from(PlatformNotification notification) {
        return new AdminNotificationItemResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getReadAt() != null,
                notification.getCreatedAt()
        );
    }
}
