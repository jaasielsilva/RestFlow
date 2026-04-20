package com.jaasielsilva.erpcorporativo.app.usecase.system.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jaasielsilva.erpcorporativo.app.model.SupportSlaPolicy;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicket;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.model.Tenant;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportEscalationRuleRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportSlaPolicyRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportTicketRepository;
import com.jaasielsilva.erpcorporativo.app.service.shared.AuditService;

@ExtendWith(MockitoExtension.class)
class SupportEscalationUseCaseTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private SupportSlaPolicyRepository supportSlaPolicyRepository;

    @Mock
    private SupportEscalationRuleRepository supportEscalationRuleRepository;

    @Mock
    private AuditService auditService;

    @Test
    void shouldEscalateTicketWhenFirstResponseSlaIsBreached() {
        SupportEscalationUseCase useCase = new SupportEscalationUseCase(
                supportTicketRepository,
                supportSlaPolicyRepository,
                supportEscalationRuleRepository,
                auditService
        );

        Tenant tenant = Tenant.builder().id(10L).nome("Tenant X").slug("tenant-x").ativo(true).build();
        SupportSlaPolicy policy = SupportSlaPolicy.builder()
                .id(1L)
                .tenant(tenant)
                .firstResponseMinutes(60)
                .resolutionMinutes(1440)
                .warningBeforeMinutes(30)
                .ativo(true)
                .build();

        SupportTicket ticket = SupportTicket.builder()
                .id(100L)
                .tenant(tenant)
                .numero("SUP-00001")
                .status(SupportTicketStatus.ABERTO)
                .prioridade(SupportTicketPriority.MEDIA)
                .firstResponseDueAt(LocalDateTime.now().minusMinutes(5))
                .resolutionDueAt(LocalDateTime.now().plusHours(1))
                .slaState(SupportSlaState.DENTRO_PRAZO)
                .build();

        when(supportSlaPolicyRepository.findAllByAtivoTrue()).thenReturn(List.of(policy));
        when(supportTicketRepository.findTicketsForEscalation(anyList(), any())).thenReturn(List.of(ticket));
        when(supportEscalationRuleRepository.findAllByTenantIdAndAtivoTrue(tenant.getId())).thenReturn(List.of());

        SupportEscalationUseCase.EscalationResult result = useCase.processEscalations();

        assertEquals(SupportSlaState.VIOLADO, ticket.getSlaState());
        assertEquals(SupportTicketPriority.CRITICA, ticket.getPrioridade());
        assertEquals(1, result.processed());
        assertEquals(1, result.violated());
        verify(supportTicketRepository).saveAll(anyList());
        verify(auditService).log(any(), any(), any(), any(), any(), any());
    }
}
