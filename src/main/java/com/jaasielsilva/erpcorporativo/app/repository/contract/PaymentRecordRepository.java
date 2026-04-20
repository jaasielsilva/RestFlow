package com.jaasielsilva.erpcorporativo.app.repository.contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findByContractIdOrderByMesReferenciaDesc(Long contractId);

    Optional<PaymentRecord> findFirstByContractIdOrderByMesReferenciaDesc(Long contractId);

    long countByStatus(PaymentStatus status);

    Optional<PaymentRecord> findByExternalReference(String externalReference);

    @Query("""
            select p from PaymentRecord p
            where p.contract.tenant.id = :tenantId
            order by p.mesReferencia desc, p.createdAt desc
            """)
    List<PaymentRecord> findAllByTenantId(@Param("tenantId") Long tenantId);
}
