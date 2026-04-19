package com.jaasielsilva.erpcorporativo.app.dto.web.os;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;

public record OrdemServicoViewModel(
        Long id,
        String numero,
        String titulo,
        String descricao,
        String clienteNome,
        String clienteEmail,
        String clienteTelefone,
        OrdemServicoStatus status,
        BigDecimal valor,
        LocalDate dataPrevista,
        String responsavelNome,
        Long responsavelId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
