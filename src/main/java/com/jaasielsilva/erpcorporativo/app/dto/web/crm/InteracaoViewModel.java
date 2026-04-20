package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import com.jaasielsilva.erpcorporativo.app.model.TipoInteracao;

import java.time.LocalDateTime;

public record InteracaoViewModel(
        Long id,
        String numero,
        Long clienteId,
        String clienteNome,
        TipoInteracao tipo,
        LocalDateTime dataInteracao,
        String assunto,
        String descricao,
        String responsavelNome,
        Long responsavelId,
        LocalDateTime createdAt
) {}
