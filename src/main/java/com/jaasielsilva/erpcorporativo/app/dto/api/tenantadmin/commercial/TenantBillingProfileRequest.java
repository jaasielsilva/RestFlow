package com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial;

import com.jaasielsilva.erpcorporativo.app.model.BillingCycle;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TenantBillingProfileRequest(
        @Email(message = "Email de faturamento inválido")
        @Size(max = 150, message = "Email de faturamento deve ter no máximo 150 caracteres")
        String billingEmail,
        @NotNull(message = "Ciclo de cobrança é obrigatório")
        BillingCycle billingCycle,
        boolean autoRenew,
        boolean selfServiceEnabled
) {
}
