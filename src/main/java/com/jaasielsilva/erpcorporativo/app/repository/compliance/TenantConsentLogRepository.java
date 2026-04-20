package com.jaasielsilva.erpcorporativo.app.repository.compliance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.TenantConsentLog;

public interface TenantConsentLogRepository extends JpaRepository<TenantConsentLog, Long> {

    List<TenantConsentLog> findTop100ByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
