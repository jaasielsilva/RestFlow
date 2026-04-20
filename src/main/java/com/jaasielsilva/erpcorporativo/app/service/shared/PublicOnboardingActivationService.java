package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.Contract;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscription;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscriptionStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.model.Role;
import com.jaasielsilva.erpcorporativo.app.model.SubscriptionPlan;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.Usuario;
import com.jaasielsilva.erpcorporativo.app.repository.contract.ContractRepository;
import com.jaasielsilva.erpcorporativo.app.repository.onboarding.OnboardingSubscriptionRepository;
import com.jaasielsilva.erpcorporativo.app.repository.plan.SubscriptionPlanRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicOnboardingActivationService {

    private final OnboardingSubscriptionRepository onboardingSubscriptionRepository;
    private final TenantRepository tenantRepository;
    private final ContractRepository contractRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantCommercialProvisioningService provisioningService;
    private final OnboardingCredentialMailService onboardingCredentialMailService;
    private final AuditService auditService;
    private final PlatformSettingService platformSettingService;

    @Transactional
    public ActivationResult activateFromPaidRecord(PaymentRecord paymentRecord, String trigger) {
        if (paymentRecord == null || paymentRecord.getStatus() != PaymentStatus.PAGO) {
            return ActivationResult.ignored("Pagamento não está aprovado.");
        }

        OnboardingSubscription onboarding = onboardingSubscriptionRepository
                .findByPaymentRecordId(paymentRecord.getId())
                .orElse(null);

        if (onboarding == null) {
            return ActivationResult.ignored("Pagamento não pertence ao onboarding público.");
        }

        if (onboarding.getStatus() == OnboardingSubscriptionStatus.ACTIVATED) {
            return ActivationResult.alreadyActivated(onboarding.getId(), onboarding.getTenantId());
        }

        Tenant tenant = tenantRepository.findById(onboarding.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant do onboarding não encontrado."));
        Contract contract = contractRepository.findById(onboarding.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato do onboarding não encontrado."));
        SubscriptionPlan plan = subscriptionPlanRepository.findById(onboarding.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano do onboarding não encontrado."));

        tenant.setAtivo(true);
        if (tenant.getSubscriptionPlan() == null) {
            tenant.setSubscriptionPlan(plan);
        }
        tenantRepository.save(tenant);

        contract.setStatus(ContractStatus.ATIVO);
        contractRepository.save(contract);

        provisioningService.ensureProvisionedForPlan(tenant, plan);

        Usuario admin = usuarioRepository.findFirstByTenantIdAndRoleOrderByIdAsc(tenant.getId(), Role.ADMIN).orElse(null);
        String temporaryPassword = null;
        if (admin == null) {
            temporaryPassword = generateTemporaryPassword();
            admin = Usuario.builder()
                    .nome(onboarding.getAdminNome())
                    .email(onboarding.getAdminEmail())
                    .password(passwordEncoder.encode(temporaryPassword))
                    .ativo(true)
                    .role(Role.ADMIN)
                    .tenant(tenant)
                    .build();
            admin = usuarioRepository.save(admin);
            auditService.log(
                    AuditAction.USUARIO_CRIADO,
                    "Admin criado automaticamente para o tenant " + tenant.getNome() + ".",
                    "Usuario",
                    admin.getId(),
                    "PUBLIC_ONBOARDING",
                    tenant
            );
        }

        onboarding.setStatus(OnboardingSubscriptionStatus.ACTIVATED);
        onboarding.setFailureReason(null);
        onboarding.setActivatedAt(LocalDateTime.now());

        try {
            if (temporaryPassword != null) {
                onboardingCredentialMailService.sendCredentials(
                        onboarding.getAdminEmail(),
                        onboarding.getAdminNome(),
                        tenant.getNome(),
                        tenant.getId(),
                        onboarding.getAdminEmail(),
                        temporaryPassword
                );
            }
        } catch (RuntimeException ex) {
            onboarding.setStatus(OnboardingSubscriptionStatus.ACTIVATED_EMAIL_PENDING);
            onboarding.setFailureReason(ex.getMessage());
            log.warn(
                    "[Onboarding] Ativação concluída sem envio de e-mail onboardingId={} tenantId={} trigger={} motivo={}",
                    onboarding.getId(),
                    tenant.getId(),
                    trigger,
                    ex.getMessage()
            );
        }

        onboardingSubscriptionRepository.save(onboarding);
        auditService.log(
                AuditAction.TENANT_ATUALIZADO,
                "Tenant ativado automaticamente após pagamento inicial. Trigger: " + trigger + ".",
                "Tenant",
                tenant.getId(),
                "PUBLIC_ONBOARDING",
                tenant
        );

        return ActivationResult.activated(
                onboarding.getId(),
                tenant.getId(),
                onboarding.getAdminEmail(),
                onboarding.getStatus() == OnboardingSubscriptionStatus.ACTIVATED
        );
    }

    @Transactional
    public ActivationResult resendCredentials(Long onboardingId, String executadoPor) {
        OnboardingSubscription onboarding = onboardingSubscriptionRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding não encontrado."));
        Tenant tenant = tenantRepository.findById(onboarding.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant do onboarding não encontrado."));
        Usuario admin = usuarioRepository.findFirstByTenantIdAndRoleOrderByIdAsc(tenant.getId(), Role.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Admin do tenant não encontrado."));

        String temporaryPassword = generateTemporaryPassword();
        admin.setPassword(passwordEncoder.encode(temporaryPassword));
        usuarioRepository.save(admin);

        onboardingCredentialMailService.sendCredentials(
                onboarding.getAdminEmail(),
                onboarding.getAdminNome(),
                tenant.getNome(),
                tenant.getId(),
                admin.getEmail(),
                temporaryPassword
        );

        onboarding.setStatus(OnboardingSubscriptionStatus.ACTIVATED);
        onboarding.setFailureReason(null);
        onboardingSubscriptionRepository.save(onboarding);

        auditService.log(
                AuditAction.USUARIO_SENHA_RESETADA,
                "Credenciais reenviadas para onboarding #" + onboardingId + ".",
                "Usuario",
                admin.getId(),
                executadoPor,
                tenant
        );

        return ActivationResult.activated(onboarding.getId(), tenant.getId(), admin.getEmail(), true);
    }

    @Transactional(readOnly = true)
    public OnboardingSubscription findByPaymentReference(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return null;
        }
        return onboardingSubscriptionRepository.findByExternalReference(externalReference).orElse(null);
    }

    private String generateTemporaryPassword() {
        byte[] raw = new byte[12];
        new SecureRandom().nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String minLenValue = platformSettingService.get(PlatformSettingService.PASSWORD_MIN_LEN, "8");
        int minLen = 8;
        try {
            minLen = Integer.parseInt(minLenValue);
        } catch (NumberFormatException ignored) {
            // fallback já definido
        }
        int target = Math.max(minLen, 12);
        return "Erp@" + token.substring(0, Math.min(token.length(), target));
    }

    public record ActivationResult(
            boolean activated,
            boolean ignored,
            boolean alreadyActive,
            Long onboardingId,
            Long tenantId,
            String adminEmail,
            boolean emailSent,
            String message
    ) {
        static ActivationResult ignored(String message) {
            return new ActivationResult(false, true, false, null, null, null, false, message);
        }

        static ActivationResult alreadyActivated(Long onboardingId, Long tenantId) {
            return new ActivationResult(false, false, true, onboardingId, tenantId, null, true, "Onboarding já ativado.");
        }

        static ActivationResult activated(Long onboardingId, Long tenantId, String adminEmail, boolean emailSent) {
            return new ActivationResult(true, false, false, onboardingId, tenantId, adminEmail, emailSent, "Onboarding ativado.");
        }
    }
}
