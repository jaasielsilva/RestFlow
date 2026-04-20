package com.jaasielsilva.erpcorporativo.app.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.WorkflowRule;

public interface WorkflowRuleRepository extends JpaRepository<WorkflowRule, Long> {

    List<WorkflowRule> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<WorkflowRule> findAllByTenantIdAndAtivoTrueAndEventType(Long tenantId, String eventType);

    List<WorkflowRule> findAllByAtivoTrueAndEventType(String eventType);
}
