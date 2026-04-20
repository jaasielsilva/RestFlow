package com.jaasielsilva.erpcorporativo.app.dto.api.admin;

import java.util.List;

public record AdminNotificationFeedResponse(
        long unreadCount,
        List<AdminNotificationItemResponse> items
) {
}
