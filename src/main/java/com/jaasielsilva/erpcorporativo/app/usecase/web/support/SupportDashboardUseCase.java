package com.jaasielsilva.erpcorporativo.app.usecase.web.support;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.web.support.SupportDashboardViewModel;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportTicketRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SupportDashboardUseCase {

    private final SupportTicketRepository supportTicketRepository;

    @Transactional(readOnly = true)
    public SupportDashboardViewModel summarize(Long tenantId, int periodDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(Math.max(periodDays, 1));

        List<SupportTicketStatus> backlogStatuses = List.of(
                SupportTicketStatus.ABERTO,
                SupportTicketStatus.EM_ATENDIMENTO,
                SupportTicketStatus.AGUARDANDO_CLIENTE
        );

        long backlog = supportTicketRepository.countByTenantIdAndStatusIn(tenantId, backlogStatuses);
        long totalAbertos = supportTicketRepository.countByTenantIdAndStatus(tenantId, SupportTicketStatus.ABERTO);
        long totalViolados = supportTicketRepository.countByTenantIdAndSlaState(tenantId, SupportSlaState.VIOLADO);

        List<SupportTicket> resolved = supportTicketRepository.findAllByTenantIdAndResolvedAtBetween(tenantId, from, now);
        long resolvedCount = resolved.size();
        double avgFirstResponseHours = resolved.stream()
                .filter(ticket -> ticket.getFirstRespondedAt() != null && ticket.getCreatedAt() != null)
                .mapToLong(ticket -> Duration.between(ticket.getCreatedAt(), ticket.getFirstRespondedAt()).toMinutes())
                .average()
                .orElse(0d) / 60d;

        double avgResolutionHours = resolved.stream()
                .filter(ticket -> ticket.getResolvedAt() != null && ticket.getCreatedAt() != null)
                .mapToLong(ticket -> Duration.between(ticket.getCreatedAt(), ticket.getResolvedAt()).toMinutes())
                .average()
                .orElse(0d) / 60d;

        long slaWithin = resolved.stream()
                .filter(ticket -> ticket.getResolutionDueAt() != null && ticket.getResolvedAt() != null)
                .filter(ticket -> !ticket.getResolvedAt().isAfter(ticket.getResolutionDueAt()))
                .count();

        double slaRate = resolvedCount == 0 ? 100d : (slaWithin * 100d / resolvedCount);

        return new SupportDashboardViewModel(
                backlog,
                totalAbertos,
                resolvedCount,
                totalViolados,
                round2(avgFirstResponseHours),
                round2(avgResolutionHours),
                round2(slaRate)
        );
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
