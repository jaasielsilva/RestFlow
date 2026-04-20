package com.jaasielsilva.erpcorporativo.app.repository.crm;

import com.jaasielsilva.erpcorporativo.app.model.Cliente;
import com.jaasielsilva.erpcorporativo.app.model.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, StatusCliente status);

    @Query("select coalesce(max(cast(substring(c.numero, 5) as int)), 0) from Cliente c where c.tenant.id = :tenantId")
    int findMaxSequenceByTenantId(@Param("tenantId") Long tenantId);

    Optional<Cliente> findByTenantIdAndDocumento(Long tenantId, String documento);

    boolean existsByTenantIdAndDocumentoAndIdNot(Long tenantId, String documento, Long id);
}
