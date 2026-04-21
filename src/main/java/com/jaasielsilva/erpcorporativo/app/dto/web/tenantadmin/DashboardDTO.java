package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class DashboardDTO {
    private BigDecimal faturamentoHoje;
    private BigDecimal faturamentoMes;
    private Double crescimentoPercentual;
    private List<TopClienteDTO> topClientes;
    private List<TopServicoDTO> topServicos;
}