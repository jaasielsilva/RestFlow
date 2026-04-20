package com.jaasielsilva.erpcorporativo.app.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaasielsilva.erpcorporativo.app.model.WorkflowExecutionLog;

public interface WorkflowExecutionLogRepository extends JpaRepository<WorkflowExecutionLog, Long> {

    List<WorkflowExecutionLog> findTop100ByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
