package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial;

import com.jaasielsilva.erpcorporativo.app.model.BillingCycle;

public record TenantBillingProfileResponse(
        String billingEmail,
        BillingCycle billingCycle,
        boolean autoRenew,
        boolean selfServiceEnabled
) {
}
