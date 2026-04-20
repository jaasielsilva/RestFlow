package com.jaasielsilva.erpcorporativo.app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tenant_onboarding_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenant_onboarding_progress_tenant", columnNames = "tenant_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantOnboardingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tenant_onboarding_progress_tenant"))
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OnboardingStatus status = OnboardingStatus.NOT_STARTED;

    @Column(name = "template_code", length = 30)
    private String templateCode;

    @Column(name = "company_profile_completed", nullable = false)
    @Builder.Default
    private boolean companyProfileCompleted = false;

    @Column(name = "team_invited", nullable = false)
    @Builder.Default
    private boolean teamInvited = false;

    @Column(name = "modules_configured", nullable = false)
    @Builder.Default
    private boolean modulesConfigured = false;

    @Column(name = "first_ticket_created", nullable = false)
    @Builder.Default
    private boolean firstTicketCreated = false;

    @Column(name = "first_order_created", nullable = false)
    @Builder.Default
    private boolean firstOrderCreated = false;

    @Column(name = "completion_percent", nullable = false)
    @Builder.Default
    private int completionPercent = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
