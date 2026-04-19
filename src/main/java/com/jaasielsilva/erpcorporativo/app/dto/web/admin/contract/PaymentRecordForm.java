package com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import com.jaasielsilva.erpcorporativo.app.model.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentRecordForm {

    @NotNull(message = "Mês de referência é obrigatório")
    private YearMonth mesReferencia;

    @NotNull(message = "Valor pago é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor pago deve ser maior ou igual a zero")
    private BigDecimal valorPago;

    private LocalDate dataPagamento;

    @NotNull(message = "Status é obrigatório")
    private PaymentStatus status = PaymentStatus.PENDENTE;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;
}
