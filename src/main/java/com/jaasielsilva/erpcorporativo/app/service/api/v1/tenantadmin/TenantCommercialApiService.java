package com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.api.admin.plan.SubscriptionPlanResponse.PlanAddonItem;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantBillingProfileRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantBillingProfileResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantCheckoutResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantCommercialProfileResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantInvoiceResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.commercial.TenantOnboardingResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.model.TenantBillingProfile;
import com.jaasielsilva.erpcorporativo.app.model.TenantOnboardingProgress;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;
import com.jaasielsilva.erpcorporativo.app.security.AppUserDetails;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;
import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantBillingProfileRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantOnboardingProgressRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class TenantCommercialApiService {

    private final TenantRepository tenantRepository;
    private final TenantBillingProfileRepository tenantBillingProfileRepository;
    private final TenantOnboardingProgressRepository tenantOnboardingProgressRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final MercadoPagoBillingService mercadoPagoBillingService;

    @Transactional
    public TenantCommercialProfileResponse profile(Authentication authentication) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        var tenant = tenantRepository.findById(currentUser.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));

        var plan = tenant.getSubscriptionPlan();
        List<PlanAddonItem> addons = plan == null
                ? List.of()
                : plan.getAddons().stream()
                .map(a -> new PlanAddonItem(a.getId(), a.getCodigo(), a.getNome()))
                .sorted((a, b) -> a.nome().compareToIgnoreCase(b.nome()))
                .toList();

        return new TenantCommercialProfileResponse(
                tenant.getId(),
                tenant.getNome(),
                plan != null ? plan.getCodigo() : null,
                plan != null ? plan.getNome() : null,
                plan != null ? plan.getTier().name() : null,
                plan != null ? plan.getMaxUsers() : null,
                plan != null ? plan.getMaxStorageGb() : null,
                plan != null && plan.isAnnualDiscountEligible(),
                addons,
                toBillingResponse(resolveBillingProfile(tenant.getId())),
                toOnboardingResponse(resolveOnboarding(tenant.getId()))
        );
    }

    @Transactional
    public TenantBillingProfileResponse updateBillingProfile(
            Authentication authentication,
            TenantBillingProfileRequest request
    ) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        TenantBillingProfile profile = resolveBillingProfile(currentUser.getTenantId());
        profile.setBillingEmail(request.billingEmail());
        profile.setBillingCycle(request.billingCycle());
        profile.setAutoRenew(request.autoRenew());
        profile.setSelfServiceEnabled(request.selfServiceEnabled());
        tenantBillingProfileRepository.save(profile);
        return toBillingResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<TenantInvoiceResponse> listInvoices(Authentication authentication) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        return paymentRecordRepository.findAllByTenantId(currentUser.getTenantId()).stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

    @Transactional
    public TenantCheckoutResponse generateCheckout(Authentication authentication, Long paymentId) {
        AppUserDetails currentUser = SecurityPrincipalUtils.getCurrentUser(authentication);
        PaymentRecord record = paymentRecordRepository.findById(paymentId)
                .filter(r -> r.getContract() != null
                        && r.getContract().getTenant() != null
                        && r.getContract().getTenant().getId().equals(currentUser.getTenantId()))
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada."));

        if (record.getStatus() == PaymentStatus.PAGO) {
            return new TenantCheckoutResponse(record.getId(), record.getCheckoutUrl());
        }

        String tenantNome = record.getContract().getTenant().getNome();
        PaymentRecord updated = mercadoPagoBillingService.createCheckout(record, tenantNome);
        return new TenantCheckoutResponse(updated.getId(), updated.getCheckoutUrl());
    }

    private TenantBillingProfile resolveBillingProfile(Long tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        return tenantBillingProfileRepository.findByTenantId(tenantId)
                .orElseGet(() -> tenantBillingProfileRepository.save(TenantBillingProfile.builder()
                        .tenant(tenant)
                        .billingEmail("financeiro@" + tenant.getSlug() + ".local")
                        .build()));
    }

    private TenantOnboardingProgress resolveOnboarding(Long tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        return tenantOnboardingProgressRepository.findByTenantId(tenantId)
                .orElseGet(() -> tenantOnboardingProgressRepository.save(TenantOnboardingProgress.builder()
                        .tenant(tenant)
                        .status(OnboardingStatus.NOT_STARTED)
                        .completionPercent(0)
                        .build()));
    }

    private TenantBillingProfileResponse toBillingResponse(TenantBillingProfile profile) {
        return new TenantBillingProfileResponse(
                profile.getBillingEmail(),
                profile.getBillingCycle(),
                profile.isAutoRenew(),
                profile.isSelfServiceEnabled()
        );
    }

    private TenantOnboardingResponse toOnboardingResponse(TenantOnboardingProgress progress) {
        return new TenantOnboardingResponse(
                progress.getStatus(),
                progress.getTemplateCode(),
                progress.isCompanyProfileCompleted(),
                progress.isTeamInvited(),
                progress.isModulesConfigured(),
                progress.isFirstTicketCreated(),
                progress.isFirstOrderCreated(),
                progress.getCompletionPercent()
        );
    }

    private TenantInvoiceResponse toInvoiceResponse(PaymentRecord record) {
        return new TenantInvoiceResponse(
                record.getId(),
                record.getMesReferencia(),
                record.getValorPago(),
                record.getStatus(),
                record.getPaymentProvider(),
                record.getDataPagamento(),
                record.getCheckoutUrl(),
                record.getCreatedAt()
        );
    }
}
