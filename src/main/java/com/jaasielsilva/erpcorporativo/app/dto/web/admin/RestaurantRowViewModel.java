package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.time.LocalDateTime;

public record RestaurantRowViewModel(
        Long id,
        String nome,
        String email,
        String plano,
        String status,
        LocalDateTime criadoEm
) {
}
