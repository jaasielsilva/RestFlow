package com.jaasielsilva.erpcorporativo.app.usecase.api.v1.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.PlanAddon;
import com.jaasielsilva.erpcorporativo.app.model.PlanTier;
import com.jaasielsilva.erpcorporativo.app.model.PlatformModule;
import com.jaasielsilva.erpcorporativo.app.model.SubscriptionPlan;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.TenantModule;
import com.jaasielsilva.erpcorporativo.app.repository.module.PlatformModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.module.TenantModuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.PlanAddonRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.SubscriptionPlanRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.service.shared.AuditService;
import com.jaasielsilva.erpcorporativo.app.service.shared.TenantCommercialProvisioningService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanAdminUseCase {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlatformModuleRepository platformModuleRepository;
    private final PlanAddonRepository planAddonRepository;
    private final TenantRepository tenantRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final AuditService auditService;
    private final TenantCommercialProvisioningService tenantCommercialProvisioningService;

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> listAll() {
        return subscriptionPlanRepository.findAll().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getById(Long id) {
        return findPlan(id);
    }

    @Transactional
    public SubscriptionPlan create(
            String codigo,
            String nome,
            String descricao,
            boolean ativo,
            PlanTier tier,
            Integer maxUsers,
            Integer maxStorageGb,
            boolean annualDiscountEligible,
            String onboardingTemplate,
            Set<Long> moduleIds,
            Set<Long> addonIds
    ) {
        validateCodigo(codigo, null);

        Set<PlatformModule> modules = resolveModules(moduleIds);
        Set<PlanAddon> addons = resolveAddons(addonIds);

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .codigo(codigo.trim())
                .nome(nome.trim())
                .descricao(descricao != null && !descricao.isBlank() ? descricao.trim() : null)
                .ativo(ativo)
                .tier(tier != null ? tier : PlanTier.START)
                .maxUsers(maxUsers)
                .maxStorageGb(maxStorageGb)
                .annualDiscountEligible(annualDiscountEligible)
                .onboardingTemplate(onboardingTemplate != null && !onboardingTemplate.isBlank() ? onboardingTemplate.trim() : null)
                .modules(modules)
                .addons(addons)
                .build();

        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public SubscriptionPlan update(
            Long id,
            String codigo,
            String nome,
            String descricao,
            boolean ativo,
            PlanTier tier,
            Integer maxUsers,
            Integer maxStorageGb,
            boolean annualDiscountEligible,
            String onboardingTemplate,
            Set<Long> moduleIds,
            Set<Long> addonIds
    ) {
        SubscriptionPlan plan = findPlan(id);
        validateCodigo(codigo, id);

        plan.setCodigo(codigo.trim());
        plan.setNome(nome.trim());
        plan.setDescricao(descricao != null && !descricao.isBlank() ? descricao.trim() : null);
        plan.setAtivo(ativo);
        plan.setTier(tier != null ? tier : PlanTier.START);
        plan.setMaxUsers(maxUsers);
        plan.setMaxStorageGb(maxStorageGb);
        plan.setAnnualDiscountEligible(annualDiscountEligible);
        plan.setOnboardingTemplate(onboardingTemplate != null && !onboardingTemplate.isBlank() ? onboardingTemplate.trim() : null);
        plan.setModules(resolveModules(moduleIds));
        plan.setAddons(resolveAddons(addonIds));

        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public void delete(Long id) {
        SubscriptionPlan plan = findPlan(id);

        boolean hasTenantsAssigned = tenantRepository.findAll().stream()
                .anyMatch(t -> t.getSubscriptionPlan() != null && t.getSubscriptionPlan().getId().equals(id));

        if (hasTenantsAssigned) {
            throw new ConflictException("Não é possível remover um plano com tenants vinculados.");
        }

        subscriptionPlanRepository.delete(plan);
    }

    /**
     * Associa um plano a um tenant e provisiona automaticamente os TenantModules
     * conforme os módulos incluídos no plano.
     */
    @Transactional
    public void assignPlanToTenant(Long tenantId, Long planId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + tenantId));

        SubscriptionPlan plan = findPlan(planId);

        tenant.setSubscriptionPlan(plan);
        tenantRepository.save(tenant);
        provisionTenantModules(tenant, plan);
        tenantCommercialProvisioningService.ensureProvisionedForPlan(tenant, plan);
        auditService.log(AuditAction.TENANT_PLANO_ATRIBUIDO,
                "Plano '" + plan.getNome() + "' atribuído ao tenant '" + tenant.getNome() + "'.",
                "Tenant", tenant.getId(), "SUPER_ADMIN", null);
    }

    /**
     * Remove o plano de um tenant (não remove os TenantModules já provisionados).
     */
    @Transactional
    public void removePlanFromTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + tenantId));

        tenant.setSubscriptionPlan(null);
        tenantRepository.save(tenant);
    }

    // -------------------------------------------------------------------------

    private void provisionTenantModules(Tenant tenant, SubscriptionPlan plan) {
        for (PlatformModule module : plan.getModules()) {
            TenantModule tm = tenantModuleRepository
                    .findByTenantIdAndModuleId(tenant.getId(), module.getId())
                    .orElseGet(() -> TenantModule.builder()
                            .tenant(tenant)
                            .module(module)
                            .ativo(false)
                            .build());

            tm.setAtivo(true);
            tenantModuleRepository.save(tm);
        }
    }

    private Set<PlatformModule> resolveModules(Set<Long> moduleIds) {
        if (moduleIds == null || moduleIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<PlatformModule> modules = new HashSet<>(platformModuleRepository.findAllById(moduleIds));

        if (modules.size() != moduleIds.size()) {
            throw new ResourceNotFoundException("Um ou mais módulos informados não foram encontrados.");
        }

        return modules;
    }

    private Set<PlanAddon> resolveAddons(Set<Long> addonIds) {
        if (addonIds == null || addonIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<PlanAddon> addons = new HashSet<>(planAddonRepository.findAllById(addonIds));
        if (addons.size() != addonIds.size()) {
            throw new ResourceNotFoundException("Um ou mais add-ons informados não foram encontrados.");
        }
        return addons;
    }

    private SubscriptionPlan findPlan(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado: " + id));
    }

    private void validateCodigo(String codigo, Long planId) {
        if (codigo == null || codigo.isBlank()) {
            throw new ConflictException("Código do plano é obrigatório.");
        }

        subscriptionPlanRepository.findByCodigoIgnoreCase(codigo.trim())
                .filter(existing -> !existing.getId().equals(planId))
                .ifPresent(existing -> {
                    throw new ConflictException("Já existe um plano com este código.");
                });
    }
}
