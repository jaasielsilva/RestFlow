package com.jaasielsilva.erpcorporativo.app.repository.integration;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.IntegrationEndpoint;

public interface IntegrationEndpointRepository extends JpaRepository<IntegrationEndpoint, Long> {

    List<IntegrationEndpoint> findAllByTenantIdOrderByNomeAsc(Long tenantId);

    List<IntegrationEndpoint> findAllByTenantIdAndAtivoTrueAndEventType(Long tenantId, String eventType);

    Optional<IntegrationEndpoint> findByIdAndTenantId(Long id, Long tenantId);
}
