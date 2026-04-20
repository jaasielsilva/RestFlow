package com.jaasielsilva.erpcorporativo.app.repository.integration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.IntegrationDeliveryLog;

public interface IntegrationDeliveryLogRepository extends JpaRepository<IntegrationDeliveryLog, Long> {

    List<IntegrationDeliveryLog> findTop50ByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
