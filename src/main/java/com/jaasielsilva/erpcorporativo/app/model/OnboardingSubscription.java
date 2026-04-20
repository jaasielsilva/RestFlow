package com.jaasielsilva.erpcorporativo.app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "onboarding_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "payment_record_id", nullable = false, unique = true)
    private Long paymentRecordId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "tenant_nome", nullable = false, length = 120)
    private String tenantNome;

    @Column(name = "tenant_slug", nullable = false, length = 80)
    private String tenantSlug;

    @Column(name = "admin_nome", nullable = false, length = 150)
    private String adminNome;

    @Column(name = "admin_email", nullable = false, length = 150)
    private String adminEmail;

    @Column(name = "external_reference", length = 120, unique = true)
    private String externalReference;

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OnboardingSubscriptionStatus status;

    @Column(name = "failure_reason", length = 300)
    private String failureReason;

    @Column(name = "origin_ip", length = 80)
    private String originIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
