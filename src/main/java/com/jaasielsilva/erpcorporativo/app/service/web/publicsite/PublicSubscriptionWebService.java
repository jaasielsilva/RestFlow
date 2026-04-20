package com.jaasielsilva.erpcorporativo.app.service.web.publicsite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
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

        String normalizedSlug = normalizeSlug(form.getTenantSlug());
        String normalizedEmail = normalizeEmail(form.getAdminEmail());
        OnboardingSubscription pending = findPendingOnboarding(normalizedSlug, normalizedEmail);
        if (pending != null) {
            return resumePendingOnboarding(pending, baseUrl);
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(form.getPlanId())
                .filter(SubscriptionPlan::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado ou inativo."));

        ensureSlugAvailable(normalizedSlug);
        ensureAdminEmailAvailable(normalizedEmail);

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
                .adminEmail(normalizedEmail)
                .status(OnboardingSubscriptionStatus.PENDING_PAYMENT)
                .originIp(originIp)
                .userAgent(userAgent)
                .build());

        String externalReference = "onb_" + onboarding.getId() + "_" + UUID.randomUUID();
        MercadoPagoBillingService.CheckoutBackUrls backUrls = resolveBackUrls(baseUrl);

        PaymentRecord updatedPayment = ensureCheckoutWithSafeFallback(
                paymentRecord,
                plan.getNome(),
                tenant.getNome(),
                backUrls,
                externalReference
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
                updatedPayment.getCheckoutUrl(),
                false
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

    private String normalizeEmail(String rawEmail) {
        if (rawEmail == null) {
            throw new ValidationException("E-mail do administrador é obrigatório.");
        }
        String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new ValidationException("E-mail do administrador é obrigatório.");
        }
        return normalized;
    }

    private OnboardingSubscription findPendingOnboarding(String normalizedSlug, String normalizedEmail) {
        List<OnboardingSubscriptionStatus> pendingStatuses = List.of(OnboardingSubscriptionStatus.PENDING_PAYMENT);
        return onboardingSubscriptionRepository
                .findFirstByTenantSlugIgnoreCaseAndStatusInOrderByCreatedAtDesc(normalizedSlug, pendingStatuses)
                .or(() -> onboardingSubscriptionRepository
                        .findFirstByAdminEmailIgnoreCaseAndStatusInOrderByCreatedAtDesc(normalizedEmail, pendingStatuses))
                .orElse(null);
    }

    private PublicSubscriptionStartResult resumePendingOnboarding(OnboardingSubscription pending, String baseUrl) {
        PaymentRecord paymentRecord = paymentRecordRepository.findById(pending.getPaymentRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento do onboarding pendente não encontrado."));

        if (paymentRecord.getStatus() == PaymentStatus.PAGO) {
            throw new ValidationException(
                    "Pagamento já aprovado para este cadastro. Aguarde a ativação automática ou contate o suporte.");
        }

        Tenant tenant = tenantRepository.findById(pending.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant do onboarding pendente não encontrado."));
        SubscriptionPlan plan = subscriptionPlanRepository.findById(pending.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano do onboarding pendente não encontrado."));

        String externalReference = (pending.getExternalReference() != null && !pending.getExternalReference().isBlank())
                ? pending.getExternalReference()
                : "onb_" + pending.getId() + "_" + UUID.randomUUID();
        MercadoPagoBillingService.CheckoutBackUrls backUrls = resolveBackUrls(baseUrl);
        PaymentRecord updatedPayment = ensureCheckoutWithSafeFallback(
                paymentRecord,
                plan.getNome(),
                tenant.getNome(),
                backUrls,
                externalReference
        );

        pending.setExternalReference(updatedPayment.getExternalReference());
        pending.setCheckoutUrl(updatedPayment.getCheckoutUrl());
        pending.setFailureReason(null);
        onboardingSubscriptionRepository.save(pending);
        auditService.log(
                AuditAction.PAGAMENTO_ATUALIZADO,
                "Checkout de onboarding regenerado para tenant '" + pending.getTenantNome() + "'.",
                "PaymentRecord",
                pending.getPaymentRecordId(),
                "PUBLIC_ONBOARDING",
                null
        );

        return new PublicSubscriptionStartResult(
                pending.getId(),
                pending.getTenantId(),
                pending.getPaymentRecordId(),
                pending.getExternalReference(),
                pending.getCheckoutUrl(),
                true
        );
    }

    private MercadoPagoBillingService.CheckoutBackUrls resolveBackUrls(String baseUrl) {
        String root = normalizeBaseUrl(baseUrl);
        if (root == null) {
            root = normalizeBaseUrl(platformSettingService.get(PlatformSettingService.MP_SUCCESS_URL, ""));
        }
        if (root == null) {
            root = "http://localhost:8080";
        }
        return new MercadoPagoBillingService.CheckoutBackUrls(
                root + "/assinatura/status?state=success",
                root + "/assinatura/status?state=failure",
                root + "/assinatura/status?state=pending"
        );
    }

    private String normalizeBaseUrl(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(candidate.trim());
            String scheme = uri.getScheme();
            String authority = uri.getAuthority();
            if (scheme == null || authority == null || authority.isBlank()) {
                return null;
            }
            return scheme + "://" + authority;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private PaymentRecord ensureCheckoutWithSafeFallback(
            PaymentRecord paymentRecord,
            String planName,
            String tenantName,
            MercadoPagoBillingService.CheckoutBackUrls backUrls,
            String externalReference
    ) {
        try {
            return mercadoPagoBillingService.createCheckout(
                    paymentRecord,
                    "Assinatura de entrada - " + planName,
                    "Ativação inicial da plataforma para " + tenantName,
                    backUrls,
                    externalReference,
                    true
            );
        } catch (ValidationException ex) {
            String message = ex.getMessage() != null ? ex.getMessage().toLowerCase(Locale.ROOT) : "";
            boolean autoReturnBackUrlError = message.contains("auto_return")
                    || message.contains("back_url.success")
                    || message.contains("back urls");
            if (!autoReturnBackUrlError) {
                throw ex;
            }
            return mercadoPagoBillingService.createCheckout(
                    paymentRecord,
                    "Assinatura de entrada - " + planName,
                    "Ativação inicial da plataforma para " + tenantName,
                    backUrls,
                    externalReference,
                    false
            );
        }
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
