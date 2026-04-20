package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.support;

public record SupportSlaPolicyResponse(
        int firstResponseMinutes,
        int resolutionMinutes,
        int warningBeforeMinutes
) {
}
