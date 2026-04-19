package com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;

public record ContractViewModel(
        Long id,
        Long tenantId,
        String tenantNome,
        Long subscriptionPlanId,
        String subscriptionPlanNome,
        BigDecimal valorMensal,
        LocalDate dataInicio,
        LocalDate dataTermino,
        ContractStatus status,
        String observacoes,
        int diaVencimento,
        boolean isVencido,
        boolean isVencendoEm30Dias,
        PaymentStatus ultimoPagamentoStatus,
        List<PaymentRecordViewModel> pagamentos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
