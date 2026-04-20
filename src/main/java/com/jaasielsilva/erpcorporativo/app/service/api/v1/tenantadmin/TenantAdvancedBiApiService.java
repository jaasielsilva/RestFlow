package com.jaasielsilva.erpcorporativo.app.service.api.v1.tenantadmin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.erpcorporativo.app.dto.api.tenantadmin.bi.TenantAdvancedBiResponse;
import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.model.StatusOportunidade;
import com.jaasielsilva.erpcorporativo.app.model.SupportSlaState;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;
import com.jaasielsilva.erpcorporativo.app.repository.crm.ClienteRepository;
import com.jaasielsilva.erpcorporativo.app.repository.crm.OportunidadeRepository;
import com.jaasielsilva.erpcorporativo.app.repository.os.OrdemServicoRepository;
import com.jaasielsilva.erpcorporativo.app.repository.support.SupportTicketRepository;
import com.jaasielsilva.erpcorporativo.app.security.SecurityPrincipalUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class TenantAdvancedBiApiService {

    private final ClienteRepository clienteRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    @Transactional(readOnly = true)
    public TenantAdvancedBiResponse summary(Authentication authentication) {
        Long tenantId = SecurityPrincipalUtils.getCurrentUser(authentication).getTenantId();
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endMonth = startMonth.plusMonths(1).minusDays(1);

        long totalClientes = clienteRepository.countByTenantId(tenantId);
        long totalOportunidadesAbertas = oportunidadeRepository.countByTenantIdAndStatusNotIn(
                tenantId,
                List.of(StatusOportunidade.FECHADO_GANHO, StatusOportunidade.FECHADO_PERDIDO)
        );
        long totalOrdensAbertas = ordemServicoRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.ABERTA)
                + ordemServicoRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.EM_ANDAMENTO)
                + ordemServicoRepository.countByTenantIdAndStatus(tenantId, OrdemServicoStatus.AGUARDANDO_CLIENTE);
        long totalChamadosAbertos = supportTicketRepository.countByTenantIdAndStatusIn(
                tenantId,
                List.of(SupportTicketStatus.ABERTO, SupportTicketStatus.EM_ATENDIMENTO, SupportTicketStatus.AGUARDANDO_CLIENTE)
        );
        long totalChamadosViolados = supportTicketRepository.countByTenantIdAndSlaState(tenantId, SupportSlaState.VIOLADO);

        BigDecimal pipelineEstimado = oportunidadeRepository.sumValorEstimadoByTenantIdAndStatusNotIn(
                tenantId,
                List.of(StatusOportunidade.FECHADO_GANHO, StatusOportunidade.FECHADO_PERDIDO)
        );

        BigDecimal receitaMensal = paymentRecordRepository.findAllByTenantId(tenantId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAGO)
                .filter(p -> p.getDataPagamento() != null
                        && !p.getDataPagamento().isBefore(startMonth)
                        && !p.getDataPagamento().isAfter(endMonth))
                .map(p -> p.getValorPago() != null ? p.getValorPago() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long ganhos = oportunidadeRepository.countByTenantIdAndStatusAndDataFechamentoRealBetween(
                tenantId, StatusOportunidade.FECHADO_GANHO, startMonth, endMonth
        );
        long fechadas = ganhos + oportunidadeRepository.countByTenantIdAndStatusAndDataFechamentoRealBetween(
                tenantId, StatusOportunidade.FECHADO_PERDIDO, startMonth, endMonth
        );
        BigDecimal taxaConversao = fechadas == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ganhos * 100.0 / fechadas).setScale(2, RoundingMode.HALF_UP);

        return new TenantAdvancedBiResponse(
                totalClientes,
                totalOportunidadesAbertas,
                totalOrdensAbertas,
                totalChamadosAbertos,
                totalChamadosViolados,
                pipelineEstimado != null ? pipelineEstimado : BigDecimal.ZERO,
                receitaMensal,
                taxaConversao
        );
    }
}
