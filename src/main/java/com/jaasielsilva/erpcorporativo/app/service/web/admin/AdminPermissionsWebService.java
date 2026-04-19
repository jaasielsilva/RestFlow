package com.jaasielsilva.erpcorporativo.app.service.web.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.AssignPlanRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminPermissionsViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.AdminPlanCreateForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.TenantPermissionMatrixViewModel;
import com.jaasielsilva.erpcorporativo.app.model.AccessLevel;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.TenantRolePermission;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.module.TenantModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.permission.TenantRolePermissionRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.service.api.v1.admin.SubscriptionPlanAdminApiService;
import com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin.TenantRolePermissionUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPermissionsWebService {

    private final SubscriptionPlanAdminApiService subscriptionPlanAdminApiService;
    private final PlatformModuleRepository platformModuleRepository;
    private final TenantRepository tenantRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final TenantRolePermissionRepository permissionRepository;
    private final TenantRolePermissionUseCase tenantRolePermissionUseCase;

    public AdminPermissionsViewModel buildViewModel() {
        List<SubscriptionPlanResponse> plans = subscriptionPlanAdminApiService.listAll();
        List<PlatformModule> modules = platformModuleRepository.findAll().stream()
                .filter(PlatformModule::isAtivo)
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .toList();
        List<Tenant> tenants = tenantRepository.findAll().stream()
                .filter(Tenant::isAtivo)
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .toList();

        return new AdminPermissionsViewModel(plans, modules, tenants);
    }

    public TenantPermissionMatrixViewModel buildMatrix(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException("Tenant não encontrado: " + tenantId));

        List<PlatformModule> enabledModules = tenantModuleRepository
                .findEnabledModulesByTenantId(tenantId)
                .stream()
                .map(tm -> tm.getModule())
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .toList();

        List<TenantRolePermission> permissions = permissionRepository.findAllByTenantId(tenantId);

        Map<Long, Map<Role, AccessLevel>> matrix = new HashMap<>();
        for (TenantRolePermission p : permissions) {
            matrix.computeIfAbsent(p.getModule().getId(), k -> new HashMap<>())
                  .put(p.getRole(), p.getAccessLevel());
        }

        return new TenantPermissionMatrixViewModel(tenant, enabledModules, matrix, AccessLevel.values());
    }

    public SubscriptionPlanResponse createPlan(AdminPlanCreateForm form) {
        SubscriptionPlanRequest request = new SubscriptionPlanRequest(
                form.getCodigo(),
                form.getNome(),
                form.getDescricao(),
                form.isAtivo(),
                form.getModuleIds()
        );
        return subscriptionPlanAdminApiService.create(request);
    }

    public void assignPlanToTenant(Long tenantId, Long planId) {
        subscriptionPlanAdminApiService.assignPlanToTenant(tenantId, new AssignPlanRequest(planId));
    }

    public void setPermission(Long tenantId, Long moduleId, Role role, AccessLevel accessLevel) {
        tenantRolePermissionUseCase.setPermission(tenantId, moduleId, role, accessLevel);
    }
}
