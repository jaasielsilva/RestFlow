package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import com.jaasielsilva.erpcorporativo.app.model.StatusOportunidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class OportunidadeForm {

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200)
    private String titulo;

    @NotNull(message = "Status é obrigatório")
    private StatusOportunidade status = StatusOportunidade.PROSPECCAO;

    private BigDecimal valorEstimado;

    private LocalDate dataPrevistaFechamento;

    private String descricao;

    private Long responsavelId;

    @Size(max = 500)
    private String motivoPerda;
}
