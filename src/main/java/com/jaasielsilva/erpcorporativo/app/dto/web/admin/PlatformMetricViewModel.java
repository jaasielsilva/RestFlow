package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

public record PlatformMetricViewModel(
        String titulo,
        String valor,
        String variacao,
        boolean positiva,
        String icone
) {
}
