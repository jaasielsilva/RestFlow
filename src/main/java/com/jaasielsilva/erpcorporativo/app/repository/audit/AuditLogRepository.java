package com.jaasielsilva.erpcorporativo.app.repository.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            select a from AuditLog a
            where (:acao is null or a.acao = :acao)
              and (:entidade is null or lower(a.entidade) = lower(:entidade))
              and (:executadoPor is null or lower(a.executadoPor) like lower(concat('%', :executadoPor, '%')))
              and (:de is null or a.createdAt >= :de)
              and (:ate is null or a.createdAt <= :ate)
            order by a.createdAt desc
            """)
    Page<AuditLog> findWithFilters(
            @Param("acao") AuditAction acao,
            @Param("entidade") String entidade,
            @Param("executadoPor") String executadoPor,
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate,
            Pageable pageable
    );

    List<AuditLog> findTop10ByOrderByCreatedAtDesc();

    List<AuditLog> findTop4ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime after);
}
