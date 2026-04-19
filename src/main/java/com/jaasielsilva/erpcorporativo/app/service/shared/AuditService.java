package com.jaasielsilva.erpcorporativo.app.service.shared;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.AuditLog;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.audit.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction acao, String descricao, String entidade, Long entidadeId,
                    String executadoPor, Tenant tenant) {
        auditLogRepository.save(AuditLog.builder()
                .acao(acao)
                .descricao(descricao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .executadoPor(executadoPor)
                .tenant(tenant)
                .build());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction acao, String descricao, String executadoPor) {
        log(acao, descricao, null, null, executadoPor, null);
    }
}
