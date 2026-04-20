package com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract;

import java.math.BigDecimal;

public record ContractKpiViewModel(
        long totalAtivos,
        long totalVencidos,
        long totalAtrasados,
        BigDecimal mrrTotal,
        BigDecimal recebidoMesAtual,
        BigDecimal aReceberMesAtual
) {}
