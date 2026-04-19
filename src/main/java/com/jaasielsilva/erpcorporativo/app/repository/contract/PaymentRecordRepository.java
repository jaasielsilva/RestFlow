package com.jaasielsilva.erpcorporativo.app.repository.contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findByContractIdOrderByMesReferenciaDesc(Long contractId);

    Optional<PaymentRecord> findFirstByContractIdOrderByMesReferenciaDesc(Long contractId);

    long countByStatus(PaymentStatus status);
}
