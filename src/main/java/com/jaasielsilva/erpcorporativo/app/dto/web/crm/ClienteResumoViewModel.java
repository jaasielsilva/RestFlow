package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import com.jaasielsilva.erpcorporativo.app.model.StatusCliente;
import com.jaasielsilva.erpcorporativo.app.model.TipoCliente;

import java.time.LocalDateTime;

public record ClienteResumoViewModel(
        Long id,
        String numero,
        TipoCliente tipo,
        String nome,
        String documentoMascarado,
        String email,
        String telefonePrincipal,
        StatusCliente status,
        LocalDateTime ultimaInteracaoEm,
        long oportunidadesAbertas
) {}
