package com.jaasielsilva.erpcorporativo.app.repository.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.PaymentRecord;
import com.jaasielsilva.erpcorporativo.app.model.PaymentProvider;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

  List<PaymentRecord> findByContractIdOrderByMesReferenciaDesc(Long contractId);

  boolean existsByContractId(Long contractId);

  Optional<PaymentRecord> findFirstByContractIdOrderByMesReferenciaDesc(Long contractId);

  long countByStatus(PaymentStatus status);

  List<PaymentRecord> findTop30ByStatusAndPaymentProviderAndProviderPaymentIdIsNotNullAndPaymentNotifiedAtIsNullOrderByUpdatedAtDesc(
      PaymentStatus status,
      PaymentProvider paymentProvider);

  @Query("""
      select coalesce(sum(p.valorPago), 0)
      from PaymentRecord p
      where p.status = :status
        and p.mesReferencia = :mesReferencia
      """)
  BigDecimal sumValorPagoByStatusAndMesReferencia(
      @Param("status") PaymentStatus status,
      @Param("mesReferencia") YearMonth mesReferencia);

  @Query("""
      select coalesce(sum(p.valorPago), 0)
      from PaymentRecord p
      where p.status in :statuses
        and p.mesReferencia = :mesReferencia
      """)
  BigDecimal sumValorPagoByStatusesAndMesReferencia(
      @Param("statuses") List<PaymentStatus> statuses,
      @Param("mesReferencia") YearMonth mesReferencia);

  Optional<PaymentRecord> findByExternalReference(String externalReference);

  @Query("""
      select p from PaymentRecord p
      where p.contract.tenant.id = :tenantId
      order by p.mesReferencia desc, p.createdAt desc
      """)
  List<PaymentRecord> findAllByTenantId(@Param("tenantId") Long tenantId);

  @Query("""
      select coalesce(sum(p.valorPago), 0)
      from PaymentRecord p
      where p.contract.tenant.id = :tenantId
        and p.status = :status
        and p.dataPagamento = :dataPagamento
      """)
  BigDecimal sumValorPagoByTenantStatusAndDataPagamento(
      @Param("tenantId") Long tenantId,
      @Param("status") PaymentStatus status,
      @Param("dataPagamento") LocalDate dataPagamento);
}
