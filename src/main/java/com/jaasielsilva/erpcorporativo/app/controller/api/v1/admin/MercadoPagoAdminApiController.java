package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.MercadoPagoReconciliationResponse;
import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoAdminApiController {

    private final MercadoPagoBillingService mercadoPagoBillingService;

    /**
     * Reconcilia manualmente um pagamento do Mercado Pago para suporte operacional.
     * Protegido por SUPER_ADMIN via SecurityConfig (/api/v1/admin/**).
     */
    @PostMapping("/reconciliar/{paymentId}")
    public ApiResponse<MercadoPagoReconciliationResponse> reconcile(@PathVariable("paymentId") String paymentId) {
        var result = mercadoPagoBillingService.reconcilePaymentByProviderId(paymentId);
        return ApiResponse.success(MercadoPagoReconciliationResponse.from(result));
    }
}
