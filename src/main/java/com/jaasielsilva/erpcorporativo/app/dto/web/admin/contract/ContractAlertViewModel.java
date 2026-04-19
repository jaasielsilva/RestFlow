package com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract;

import java.time.LocalDate;

public record ContractAlertViewModel(
        Long contractId,
        String tenantNome,
        LocalDate dataTermino,
        boolean isVencendo,
        boolean isPagamentoAtrasado
) {}
