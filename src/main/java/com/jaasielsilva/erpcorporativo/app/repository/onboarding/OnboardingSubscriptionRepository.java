package com.jaasielsilva.erpcorporativo.app.repository.onboarding;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscription;
import com.jaasielsilva.erpcorporativo.app.model.OnboardingSubscriptionStatus;

public interface OnboardingSubscriptionRepository extends JpaRepository<OnboardingSubscription, Long> {

    Optional<OnboardingSubscription> findByPaymentRecordId(Long paymentRecordId);

    Optional<OnboardingSubscription> findByExternalReference(String externalReference);

    boolean existsByTenantSlugIgnoreCaseAndStatusIn(String tenantSlug, List<OnboardingSubscriptionStatus> statuses);

    boolean existsByAdminEmailIgnoreCaseAndStatusIn(String adminEmail, List<OnboardingSubscriptionStatus> statuses);

    long countByOriginIpAndCreatedAtAfter(String originIp, LocalDateTime createdAt);

    List<OnboardingSubscription> findTop20ByOrderByCreatedAtDesc();
}
