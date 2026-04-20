package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.jaasielsilva.erpcorporativo.app.model.PaymentProvider;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;

public record TenantInvoiceResponse(
        Long id,
        YearMonth referencia,
        BigDecimal valor,
        PaymentStatus status,
        PaymentProvider provider,
        LocalDate dataPagamento,
        String checkoutUrl,
        LocalDateTime createdAt
) {
}
