package com.jaasielsilva.erpcorporativo.app.repository.module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.TenantModule;

public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {

    List<TenantModule> findAllByTenantId(Long tenantId);

    Optional<TenantModule> findByTenantIdAndModuleId(Long tenantId, Long moduleId);

    boolean existsByTenantIdAndModuleCodigoIgnoreCaseAndAtivoTrue(Long tenantId, String codigo);
}
