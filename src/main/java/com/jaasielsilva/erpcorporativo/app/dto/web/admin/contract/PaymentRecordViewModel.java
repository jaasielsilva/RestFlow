package com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;

public record PaymentRecordViewModel(
        Long id,
        YearMonth mesReferencia,
        BigDecimal valorPago,
        LocalDate dataPagamento,
        PaymentStatus status,
        boolean isAtrasado,
        String observacoes,
        LocalDateTime createdAt
) {}
