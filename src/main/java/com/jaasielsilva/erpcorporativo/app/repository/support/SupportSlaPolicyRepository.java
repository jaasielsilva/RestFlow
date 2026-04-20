package com.jaasielsilva.erpcorporativo.app.repository.support;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaPolicy;

public interface SupportSlaPolicyRepository extends JpaRepository<SupportSlaPolicy, Long> {

    Optional<SupportSlaPolicy> findByTenantId(Long tenantId);

    java.util.List<SupportSlaPolicy> findAllByAtivoTrue();
}
