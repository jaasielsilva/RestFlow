package com.jaasielsilva.erpcorporativo.app.repository.crm;

import com.jaasielsilva.erpcorporativo.app.model.Oportunidade;
import com.jaasielsilva.erpcorporativo.app.model.StatusOportunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OportunidadeRepository extends JpaRepository<Oportunidade, Long>, JpaSpecificationExecutor<Oportunidade> {

    List<Oportunidade> findByClienteIdOrderByCreatedAtDesc(Long clienteId);

    long countByTenantIdAndStatusNotIn(Long tenantId, List<StatusOportunidade> statuses);

    @Query("select coalesce(sum(o.valorEstimado), 0) from Oportunidade o where o.tenant.id = :tenantId and o.status not in :statuses")
    BigDecimal sumValorEstimadoByTenantIdAndStatusNotIn(@Param("tenantId") Long tenantId, @Param("statuses") List<StatusOportunidade> statuses);

    long countByTenantIdAndStatusAndDataFechamentoRealBetween(Long tenantId, StatusOportunidade status, LocalDate inicio, LocalDate fim);

    long countByTenantIdAndStatusIn(Long tenantId, List<StatusOportunidade> statuses);

    @Query("select coalesce(max(cast(substring(o.numero, 5) as int)), 0) from Oportunidade o where o.tenant.id = :tenantId")
    int findMaxSequenceByTenantId(@Param("tenantId") Long tenantId);

    long countByClienteIdAndStatusNotIn(Long clienteId, List<StatusOportunidade> statuses);
}
