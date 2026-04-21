package com.jaasielsilva.erpcorporativo.app.service.web.tenantadmin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin.DashboardDTO;
import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import com.jaasielsilva.erpcorporativo.app.repository.contract.PaymentRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantReportsService {

    private final PaymentRecordRepository paymentRecordRepository;

    public DashboardDTO getResumo(Long tenantId) {
        DashboardDTO dto = new DashboardDTO();

        BigDecimal faturamentoHoje = paymentRecordRepository
                .sumValorPagoByTenantStatusAndDataPagamento(tenantId, PaymentStatus.PAGO, LocalDate.now());

        dto.setFaturamentoHoje(faturamentoHoje);
        dto.setFaturamentoMes(BigDecimal.ZERO);
        dto.setCrescimentoPercentual(0.0);
        dto.setTopClientes(new ArrayList<>());
        dto.setTopServicos(new ArrayList<>());

        return dto;
    }
}
