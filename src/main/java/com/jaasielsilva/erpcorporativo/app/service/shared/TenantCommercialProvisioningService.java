package com.jaasielsilva.erpcorporativo.app.service.shared;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.BillingCycle;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingStatus;
import com.jaasielsilva.erpcorporativo.app.model.SubscriptionPlan;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.TenantBillingProfile;
import com.jaasielsilva.erpcorporativo.app.model.TenantOnboardingProgress;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantBillingProfileRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantOnboardingProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantCommercialProvisioningService {

    private final TenantBillingProfileRepository billingProfileRepository;
    private final TenantOnboardingProgressRepository onboardingProgressRepository;

    @Transactional
    public void ensureProvisionedForPlan(Tenant tenant, SubscriptionPlan plan) {
        ensureBillingProfile(tenant);
        ensureOnboardingProgress(tenant, plan);
    }

    private void ensureBillingProfile(Tenant tenant) {
        TenantBillingProfile profile = billingProfileRepository.findByTenantId(tenant.getId())
                .orElseGet(() -> TenantBillingProfile.builder()
                        .tenant(tenant)
                        .billingCycle(BillingCycle.MENSAL)
                        .autoRenew(true)
                        .selfServiceEnabled(true)
                        .build());

        if (profile.getBillingEmail() == null || profile.getBillingEmail().isBlank()) {
            profile.setBillingEmail("financeiro@" + tenant.getSlug() + ".local");
        }
        billingProfileRepository.save(profile);
    }

    private void ensureOnboardingProgress(Tenant tenant, SubscriptionPlan plan) {
        TenantOnboardingProgress progress = onboardingProgressRepository.findByTenantId(tenant.getId())
                .orElseGet(() -> TenantOnboardingProgress.builder()
                        .tenant(tenant)
                        .status(OnboardingStatus.NOT_STARTED)
                        .completionPercent(0)
                        .build());

        progress.setTemplateCode(plan.getOnboardingTemplate() != null && !plan.getOnboardingTemplate().isBlank()
                ? plan.getOnboardingTemplate()
                : plan.getTier().name());

        if (progress.getStatus() == OnboardingStatus.NOT_STARTED) {
            progress.setStatus(OnboardingStatus.IN_PROGRESS);
        }

        onboardingProgressRepository.save(progress);
    }
}
