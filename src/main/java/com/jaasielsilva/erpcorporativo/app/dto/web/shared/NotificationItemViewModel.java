package com.jaasielsilva.erpcorporativo.app.dto.web.shared;

import java.time.LocalDateTime;

import com.jaasielsilva.erpcorporativo.app.model.PlatformNotification;

public record NotificationItemViewModel(
        Long id,
        String title,
        String message,
        String type,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationItemViewModel from(PlatformNotification n) {
        return new NotificationItemViewModel(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getReadAt() != null,
                n.getCreatedAt()
        );
    }
}
