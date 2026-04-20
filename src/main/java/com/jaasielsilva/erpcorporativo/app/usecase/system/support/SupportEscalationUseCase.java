package com.jaasielsilva.erpcorporativo.app.usecase.system.support;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.model.AuditAction;
import com.jaasielsilva.erpcorporativo.app.model.SupportEscalationRule;
import com.jaasielsilva.erpcorporativo.app.model.SupportEscalationTrigger;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaPolicy;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportEscalationRuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportSlaPolicyRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportTicketRepository;
import com.jaasielsilva.erpcorporativo.app.service.shared.AuditService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SupportEscalationUseCase {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportSlaPolicyRepository supportSlaPolicyRepository;
    private final SupportEscalationRuleRepository supportEscalationRuleRepository;
    private final AuditService auditService;

    @Transactional
    public EscalationResult processEscalations() {
        LocalDateTime now = LocalDateTime.now();
        int processed = 0;
        int violated = 0;
        int warned = 0;

        List<SupportSlaPolicy> policies = supportSlaPolicyRepository.findAllByAtivoTrue();
        List<SupportTicketStatus> activeStatuses = List.of(
                SupportTicketStatus.ABERTO,
                SupportTicketStatus.EM_ATENDIMENTO,
                SupportTicketStatus.AGUARDANDO_CLIENTE
        );

        for (SupportSlaPolicy policy : policies) {
            LocalDateTime threshold = now.plusMinutes(policy.getWarningBeforeMinutes());
            List<SupportTicket> candidateTickets = supportTicketRepository.findTicketsForEscalation(activeStatuses, threshold)
                    .stream()
                    .filter(ticket -> ticket.getTenant() != null && ticket.getTenant().getId().equals(policy.getTenant().getId()))
                    .toList();

            List<SupportEscalationRule> rules = supportEscalationRuleRepository.findAllByTenantIdAndAtivoTrue(policy.getTenant().getId());

            for (SupportTicket ticket : candidateTickets) {
                processed++;
                boolean firstResponseBreached = ticket.getFirstRespondedAt() == null
                        && ticket.getFirstResponseDueAt() != null
                        && now.isAfter(ticket.getFirstResponseDueAt());
                boolean resolutionBreached = ticket.getResolutionDueAt() != null && now.isAfter(ticket.getResolutionDueAt());

                if (firstResponseBreached || resolutionBreached) {
                    ticket.setSlaState(SupportSlaState.VIOLADO);
                    ticket.setPrioridade(SupportTicketPriority.CRITICA);
                    applyRules(ticket, rules, resolutionBreached
                            ? SupportEscalationTrigger.VIOLACAO_RESOLUCAO
                            : SupportEscalationTrigger.VIOLACAO_PRIMEIRA_RESPOSTA);
                    violated++;
                    auditService.log(
                            AuditAction.SUPORTE_CHAMADO_ESCALONADO,
                            "Chamado " + ticket.getNumero() + " escalonado por violação de SLA.",
                            "SupportTicket",
                            ticket.getId(),
                            "SYSTEM_SLA",
                            ticket.getTenant()
                    );
                    continue;
                }

                if ((ticket.getFirstRespondedAt() == null && ticket.getFirstResponseDueAt() != null
                        && !now.isBefore(ticket.getFirstResponseDueAt().minusMinutes(policy.getWarningBeforeMinutes())))
                        || (ticket.getResolutionDueAt() != null
                        && !now.isBefore(ticket.getResolutionDueAt().minusMinutes(policy.getWarningBeforeMinutes())))) {
                    ticket.setSlaState(SupportSlaState.PROXIMO_VENCIMENTO);
                    applyRules(ticket, rules, SupportEscalationTrigger.AVISO_SLA);
                    warned++;
                } else {
                    ticket.setSlaState(SupportSlaState.DENTRO_PRAZO);
                }
            }

            supportTicketRepository.saveAll(candidateTickets);
        }

        return new EscalationResult(processed, warned, violated);
    }

    private void applyRules(SupportTicket ticket, List<SupportEscalationRule> rules, SupportEscalationTrigger trigger) {
        rules.stream()
                .filter(rule -> rule.getGatilho() == trigger)
                .forEach(rule -> {
                    if (rule.getSetPriority() != null) {
                        ticket.setPrioridade(rule.getSetPriority());
                    }
                    if (rule.getSetStatus() != null) {
                        ticket.setStatus(rule.getSetStatus());
                    }
                    if (rule.getAssignToUser() != null) {
                        ticket.setResponsavel(rule.getAssignToUser());
                    }
                });
    }

    public record EscalationResult(
            int processed,
            int warned,
            int violated
    ) {
    }
}
