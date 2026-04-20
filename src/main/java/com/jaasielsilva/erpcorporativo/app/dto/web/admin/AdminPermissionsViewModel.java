package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import java.util.List;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanResponse;
import com.jaasielsilva.erpcorporativo.app.model.PlanAddon;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;

public record AdminPermissionsViewModel(
        List<SubscriptionPlanResponse> plans,
        List<PlatformModule> availableModules,
        List<PlanAddon> availableAddons,
        List<Tenant> tenants
) {
}
