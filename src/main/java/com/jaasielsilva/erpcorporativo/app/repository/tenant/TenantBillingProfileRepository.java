package com.jaasielsilva.erpcorporativo.app.repository.tenant;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.TenantBillingProfile;

public interface TenantBillingProfileRepository extends JpaRepository<TenantBillingProfile, Long> {

    Optional<TenantBillingProfile> findByTenantId(Long tenantId);
}
