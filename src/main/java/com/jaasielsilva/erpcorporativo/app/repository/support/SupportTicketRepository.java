package com.jaasielsilva.erpcorporativo.app.repository.support;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long>, JpaSpecificationExecutor<SupportTicket> {

    Optional<SupportTicket> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, SupportTicketStatus status);

    long countByTenantIdAndSlaState(Long tenantId, SupportSlaState slaState);

    long countByTenantIdAndStatusIn(Long tenantId, List<SupportTicketStatus> statuses);

    List<SupportTicket> findAllByTenantIdAndStatusIn(Long tenantId, List<SupportTicketStatus> statuses);

    List<SupportTicket> findAllByTenantIdAndResolvedAtBetween(Long tenantId, LocalDateTime de, LocalDateTime ate);

    @Query("""
            select t from SupportTicket t
            where t.status in :statuses
              and (
                    (t.firstRespondedAt is null and t.firstResponseDueAt is not null and t.firstResponseDueAt <= :threshold)
                    or
                    (t.resolutionDueAt is not null and t.resolutionDueAt <= :threshold)
                  )
            """)
    List<SupportTicket> findTicketsForEscalation(
            @Param("statuses") List<SupportTicketStatus> statuses,
            @Param("threshold") LocalDateTime threshold
    );

    @Query("select coalesce(max(cast(substring(t.numero, 5) as int)), 0) from SupportTicket t where t.tenant.id = :tenantId")
    int findMaxSequenceByTenantId(@Param("tenantId") Long tenantId);
}
