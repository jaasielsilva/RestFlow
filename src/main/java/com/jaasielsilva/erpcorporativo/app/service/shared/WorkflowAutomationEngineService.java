package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.model.WorkflowExecutionLog;
import com.jaasielsilva.erpcorporativo.app.model.WorkflowRule;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportTicketRepository;
import com.jaasielsilva.erpcorporativo.app.repository.tenant.TenantRepository;
import com.jaasielsilva.erpcorporativo.app.repository.workflow.WorkflowExecutionLogRepository;
import com.jaasielsilva.erpcorporativo.app.repository.workflow.WorkflowRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowAutomationEngineService {

    private final TenantRepository tenantRepository;
    private final WorkflowRuleRepository workflowRuleRepository;
    private final WorkflowExecutionLogRepository workflowExecutionLogRepository;
    private final SupportTicketRepository supportTicketRepository;

    @Transactional
    public int runScheduledAutomations() {
        int executions = 0;
        for (Tenant tenant : tenantRepository.findAllByAtivoTrue()) {
            List<WorkflowRule> rules = workflowRuleRepository.findAllByTenantIdAndAtivoTrueAndEventType(
                    tenant.getId(),
                    "support.sla_violated"
            );
            if (rules.isEmpty()) {
                continue;
            }
            List<SupportTicket> violatedTickets = supportTicketRepository.findAllByTenantIdAndStatusIn(
                    tenant.getId(),
                    List.of(SupportTicketStatus.ABERTO, SupportTicketStatus.EM_ATENDIMENTO, SupportTicketStatus.AGUARDANDO_CLIENTE)
            ).stream().filter(ticket -> ticket.getSlaState() == SupportSlaState.VIOLADO).toList();

            for (WorkflowRule rule : rules) {
                for (SupportTicket ticket : violatedTickets) {
                    applyRule(rule, ticket, tenant);
                    executions++;
                }
            }
            supportTicketRepository.saveAll(violatedTickets);
        }
        return executions;
    }

    private void applyRule(WorkflowRule rule, SupportTicket ticket, Tenant tenant) {
        boolean success = true;
        String message = "Regra aplicada.";
        try {
            if ("support.set_priority_critical".equalsIgnoreCase(rule.getActionType())) {
                ticket.setPrioridade(SupportTicketPriority.CRITICA);
            }
            if ("support.set_status_em_atendimento".equalsIgnoreCase(rule.getActionType())) {
                ticket.setStatus(SupportTicketStatus.EM_ATENDIMENTO);
            }
            if ("support.assign_first_admin".equalsIgnoreCase(rule.getActionType())) {
                // Place-holder: próxima fase poderá resolver usuário destino por payload.
                message = "Ação de atribuição marcada para implementação avançada.";
            }
        } catch (Exception ex) {
            success = false;
            message = ex.getMessage();
        }

        workflowExecutionLogRepository.save(WorkflowExecutionLog.builder()
                .tenant(tenant)
                .rule(rule)
                .eventType(rule.getEventType())
                .entityType("SupportTicket")
                .entityId(ticket.getId())
                .success(success)
                .message(message)
                .build());
    }
}
