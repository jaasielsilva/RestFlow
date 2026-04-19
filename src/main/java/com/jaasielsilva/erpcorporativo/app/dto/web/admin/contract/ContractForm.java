package com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContractForm {

    @NotNull(message = "Tenant é obrigatório")
    private Long tenantId;

    @NotNull(message = "Plano é obrigatório")
    private Long subscriptionPlanId;

    @NotNull(message = "Valor mensal é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor mensal deve ser maior ou igual a zero")
    private BigDecimal valorMensal;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;

    private LocalDate dataTermino;

    @NotNull(message = "Status é obrigatório")
    private ContractStatus status = ContractStatus.ATIVO;

    @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
    private String observacoes;

    /** Dia do mês para vencimento (1-28). Padrão: 1 */
    private int diaVencimento = 1;
}
