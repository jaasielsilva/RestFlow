package com.jaasielsilva.erpcorporativo.app.service.web.publicsite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.web.publicsite.PublicSubscriptionForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.publicsite.PublicSubscriptionStartResult;
import com.jaasielsilva.erpcorporativo.app.exception.ConflictException;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;
import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.Contract;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscription;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscriptionStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentProvider;
import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.model.SubscriptionPlan;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.contract.ContractRepository;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;
import com.jaasielsilva.erpcorporativo.app.repository.onboarding.OnboardingSubscriptionRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.SubscriptionPlanRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.service.shared.AuditService;
import com.jaasielsilva.erpcorporativo.app.service.shared.MercadoPagoBillingService;
import com.jaasielsilva.erpcorporativo.app.service.shared.PlatformSettingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicSubscriptionWebService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantRepository tenantRepository;
    private final ContractRepository contractRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final OnboardingSubscriptionRepository onboardingSubscriptionRepository;
    private final MercadoPagoBillingService mercadoPagoBillingService;
    private final PlatformSettingService platformSettingService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> listActivePlans() {
        return subscriptionPlanRepository.findAll().stream()
                .filter(SubscriptionPlan::isAtivo)
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal resolveEntryPrice() {
        String configured = platformSettingService.get(PlatformSettingService.ONBOARDING_ENTRY_PRICE, "2.00");
        try {
            BigDecimal parsed = new BigDecimal(configured).setScale(2, RoundingMode.HALF_UP);
            if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
                return new BigDecimal("2.00");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            return new BigDecimal("2.00");
        }
    }

    @Transactional
    public PublicSubscriptionStartResult startSubscription(
            PublicSubscriptionForm form,
            String baseUrl,
            String originIp,
            String userAgent
    ) {
        validateBotTrap(form);
        validateRateLimitByIp(originIp);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(form.getPlanId())
                .filter(SubscriptionPlan::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado ou inativo."));

        String normalizedSlug = normalizeSlug(form.getTenantSlug());
        ensureSlugAvailable(normalizedSlug);
        ensureAdminEmailAvailable(form.getAdminEmail());

        BigDecimal entryPrice = resolveEntryPrice();
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .nome(form.getTenantNome().trim())
                .slug(normalizedSlug)
                .ativo(false)
                .subscriptionPlan(plan)
                .build());

        Contract contract = contractRepository.save(Contract.builder()
                .tenant(tenant)
                .subscriptionPlan(plan)
                .valorMensal(entryPrice)
                .dataInicio(LocalDate.now())
                .status(ContractStatus.AGUARDANDO_ASSINATURA)
                .diaVencimento(Math.min(LocalDate.now().getDayOfMonth(), 28))
                .observacoes("Contrato criado automaticamente via onboarding público.")
                .build());

        PaymentRecord paymentRecord = paymentRecordRepository.save(PaymentRecord.builder()
                .contract(contract)
                .mesReferencia(YearMonth.now())
                .valorPago(entryPrice)
                .status(PaymentStatus.PENDENTE)
                .paymentProvider(PaymentProvider.MERCADO_PAGO)
                .observacoes("Primeira assinatura gerada automaticamente.")
                .build());

        OnboardingSubscription onboarding = onboardingSubscriptionRepository.save(OnboardingSubscription.builder()
                .tenantId(tenant.getId())
                .contractId(contract.getId())
                .paymentRecordId(paymentRecord.getId())
                .planId(plan.getId())
                .tenantNome(form.getTenantNome().trim())
                .tenantSlug(normalizedSlug)
                .adminNome(form.getAdminNome().trim())
                .adminEmail(form.getAdminEmail().trim().toLowerCase(Locale.ROOT))
                .status(OnboardingSubscriptionStatus.PENDING_PAYMENT)
                .originIp(originIp)
                .userAgent(userAgent)
                .build());

        String externalReference = "onb_" + onboarding.getId() + "_" + UUID.randomUUID();
        String successUrl = baseUrl + "/assinatura/status?state=success";
        String failureUrl = baseUrl + "/assinatura/status?state=failure";
        String pendingUrl = baseUrl + "/assinatura/status?state=pending";

        PaymentRecord updatedPayment = mercadoPagoBillingService.createCheckout(
                paymentRecord,
                "Assinatura de entrada - " + plan.getNome(),
                "Ativação inicial da plataforma para " + tenant.getNome(),
                new MercadoPagoBillingService.CheckoutBackUrls(successUrl, failureUrl, pendingUrl),
                externalReference,
                false
        );

        onboarding.setExternalReference(updatedPayment.getExternalReference());
        onboarding.setCheckoutUrl(updatedPayment.getCheckoutUrl());
        onboardingSubscriptionRepository.save(onboarding);

        auditService.log(
                AuditAction.TENANT_CRIADO,
                "Pré-cadastro público criado para o tenant '" + tenant.getNome() + "'.",
                "Tenant",
                tenant.getId(),
                "PUBLIC_ONBOARDING",
                tenant
        );
        auditService.log(
                AuditAction.PAGAMENTO_REGISTRADO,
                "Primeira cobrança do onboarding criada para tenant '" + tenant.getNome() + "'.",
                "PaymentRecord",
                updatedPayment.getId(),
                "PUBLIC_ONBOARDING",
                tenant
        );

        return new PublicSubscriptionStartResult(
                onboarding.getId(),
                tenant.getId(),
                updatedPayment.getId(),
                updatedPayment.getExternalReference(),
                updatedPayment.getCheckoutUrl()
        );
    }

    @Transactional(readOnly = true)
    public OnboardingSubscription findByExternalReference(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return null;
        }
        return onboardingSubscriptionRepository.findByExternalReference(externalReference).orElse(null);
    }

    private void validateBotTrap(PublicSubscriptionForm form) {
        if (form.getWebsite() != null && !form.getWebsite().isBlank()) {
            throw new ValidationException("Não foi possível processar a assinatura.");
        }
    }

    private void validateRateLimitByIp(String originIp) {
        if (originIp == null || originIp.isBlank()) {
            return;
        }
        long attempts = onboardingSubscriptionRepository.countByOriginIpAndCreatedAtAfter(
                originIp,
                LocalDateTime.now().minusMinutes(10)
        );
        if (attempts >= 5) {
            throw new ValidationException("Muitas tentativas de assinatura. Aguarde alguns minutos e tente novamente.");
        }
    }

    private String normalizeSlug(String rawSlug) {
        String normalized = rawSlug == null ? "" : rawSlug.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw new ValidationException("Slug inválido. Use letras minúsculas, números e hífen.");
        }
        return normalized;
    }

    private void ensureSlugAvailable(String slug) {
        if (tenantRepository.existsBySlugIgnoreCase(slug)) {
            throw new ConflictException("Este identificador de tenant já está em uso.");
        }
        boolean pendingSlug = onboardingSubscriptionRepository.existsByTenantSlugIgnoreCaseAndStatusIn(
                slug,
                List.of(
                        OnboardingSubscriptionStatus.PENDING_PAYMENT,
                        OnboardingSubscriptionStatus.ACTIVATED,
                        OnboardingSubscriptionStatus.ACTIVATED_EMAIL_PENDING
                )
        );
        if (pendingSlug) {
            throw new ConflictException("Já existe um onboarding em andamento para este identificador.");
        }
    }

    private void ensureAdminEmailAvailable(String adminEmail) {
        boolean pendingEmail = onboardingSubscriptionRepository.existsByAdminEmailIgnoreCaseAndStatusIn(
                adminEmail,
                List.of(OnboardingSubscriptionStatus.PENDING_PAYMENT)
        );
        if (pendingEmail) {
            throw new ConflictException("Já existe uma assinatura em andamento para este e-mail.");
        }
    }
}
