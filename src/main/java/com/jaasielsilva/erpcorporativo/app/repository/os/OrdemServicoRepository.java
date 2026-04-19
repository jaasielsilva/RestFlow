package com.jaasielsilva.erpcorporativo.app.repository.os;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.OrdemServico;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long>, JpaSpecificationExecutor<OrdemServico> {

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, OrdemServicoStatus status);

    @Query("select coalesce(max(cast(substring(o.numero, 4) as int)), 0) from OrdemServico o where o.tenant.id = :tenantId")
    int findMaxSequenceByTenantId(@Param("tenantId") Long tenantId);
}
