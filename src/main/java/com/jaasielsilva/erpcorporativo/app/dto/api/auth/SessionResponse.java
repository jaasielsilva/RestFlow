package com.jaasielsilva.erpcorporativo.app.dto.api.auth;

import java.util.List;

public record SessionResponse(
        String email,
        Long tenantId,
        List<String> roles
) {
}
