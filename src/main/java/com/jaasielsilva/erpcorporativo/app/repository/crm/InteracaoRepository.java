package com.jaasielsilva.erpcorporativo.app.repository.crm;

import com.jaasielsilva.erpcorporativo.app.model.Interacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InteracaoRepository extends JpaRepository<Interacao, Long> {

    List<Interacao> findByClienteIdOrderByDataInteracaoDesc(Long clienteId);

    Page<Interacao> findByClienteIdOrderByDataInteracaoDesc(Long clienteId, Pageable pageable);

    @Query("select coalesce(max(cast(substring(i.numero, 5) as int)), 0) from Interacao i where i.tenant.id = :tenantId")
    int findMaxSequenceByTenantId(@Param("tenantId") Long tenantId);
}
