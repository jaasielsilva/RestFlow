package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import com.jaasielsilva.erpcorporativo.app.model.StatusOportunidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OportunidadeViewModel(
        Long id,
        String numero,
        Long clienteId,
        String clienteNome,
        String titulo,
        StatusOportunidade status,
        BigDecimal valorEstimado,
        LocalDate dataPrevistaFechamento,
        LocalDate dataFechamentoReal,
        String motivoPerda,
        String descricao,
        String responsavelNome,
        Long responsavelId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
