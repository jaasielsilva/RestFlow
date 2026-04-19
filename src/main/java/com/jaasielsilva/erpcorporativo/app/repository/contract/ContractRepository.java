package com.jaasielsilva.erpcorporativo.app.repository.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.Contract;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;

public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    boolean existsByTenantIdAndStatus(Long tenantId, ContractStatus status);

    long countByStatus(ContractStatus status);

    long countByStatusAndDataTerminoBefore(ContractStatus status, LocalDate date);

    @Query("select coalesce(sum(c.valorMensal), 0) from Contract c where c.status = :status")
    BigDecimal sumValorMensalByStatus(@Param("status") ContractStatus status);

    List<Contract> findByStatusAndDataTerminoBetween(ContractStatus status, LocalDate from, LocalDate to);

    Optional<Contract> findFirstByTenantIdAndStatus(Long tenantId, ContractStatus status);
}
