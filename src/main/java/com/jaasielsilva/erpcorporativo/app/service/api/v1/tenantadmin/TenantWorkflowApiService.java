package com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow.WorkflowExecutionLogResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow.WorkflowRuleRequest;
import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.workflow.WorkflowRuleResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ResourceNotFoundException;
import com.jaasielsilva.erpcorporativo.app.model.WorkflowRule;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.workflow.WorkflowExecutionLogRepository;
import com.jaasielsilva.erpcorporativo.app.repository.workflow.WorkflowRuleRepository;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class TenantWorkflowApiService {

    private final WorkflowRuleRepository workflowRuleRepository;
    private final WorkflowExecutionLogRepository workflowExecutionLogRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public List<WorkflowRuleResponse> list(Authentication authentication) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        return workflowRuleRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WorkflowRuleResponse create(Authentication authentication, WorkflowRuleRequest request) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));
        WorkflowRule rule = WorkflowRule.builder()
                .tenant(tenant)
                .nome(request.nome())
                .eventType(request.eventType())
                .conditionExpression(request.conditionExpression())
                .actionType(request.actionType())
                .actionPayload(request.actionPayload())
                .ativo(request.ativo())
                .build();
        return toResponse(workflowRuleRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public List<WorkflowExecutionLogResponse> logs(Authentication authentication) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        return workflowExecutionLogRepository.findTop100ByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(log -> new WorkflowExecutionLogResponse(
                        log.getId(),
                        log.getRule() != null ? log.getRule().getId() : null,
                        log.getRule() != null ? log.getRule().getNome() : null,
                        log.getEventType(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.isSuccess(),
                        log.getMessage(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    private WorkflowRuleResponse toResponse(WorkflowRule rule) {
        return new WorkflowRuleResponse(
                rule.getId(),
                rule.getNome(),
                rule.getEventType(),
                rule.getConditionExpression(),
                rule.getActionType(),
                rule.getActionPayload(),
                rule.isAtivo(),
                rule.getCreatedAt()
        );
    }
}
