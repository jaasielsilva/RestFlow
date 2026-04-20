package com.jaasielsilva.erpcorporativo.app.dto.api.admin;

import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService.PaymentSyncResult;

public record MercadoPagoReconciliationResponse(
        String paymentId,
        String externalReference,
        Long paymentRecordId,
        String mercadoPagoStatus,
        PaymentStatus localStatusBefore,
        PaymentStatus localStatusAfter,
        boolean changed,
        boolean synced,
        String message
) {
    public static MercadoPagoReconciliationResponse from(PaymentSyncResult result) {
        return new MercadoPagoReconciliationResponse(
                result.paymentId(),
                result.externalReference(),
                result.paymentRecordId(),
                result.mercadoPagoStatus(),
                result.localStatusBefore(),
                result.localStatusAfter(),
                result.changed(),
                result.synced(),
                result.message()
        );
    }
}
