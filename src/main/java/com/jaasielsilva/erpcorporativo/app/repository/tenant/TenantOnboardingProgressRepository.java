package com.jaasielsilva.erpcorporativo.app.repository.tenant;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.TenantOnboardingProgress;

public interface TenantOnboardingProgressRepository extends JpaRepository<TenantOnboardingProgress, Long> {

    Optional<TenantOnboardingProgress> findByTenantId(Long tenantId);
}
