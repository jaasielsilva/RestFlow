package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial;

public record TenantCheckoutResponse(
        Long paymentId,
        String checkoutUrl
) {
}
