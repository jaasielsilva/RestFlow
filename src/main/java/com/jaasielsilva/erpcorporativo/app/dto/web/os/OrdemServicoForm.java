package com.jaasielsilva.erpcorporativo.app.dto.web.os;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.jaasielsilva.erpcorporativo.app.model.OrdemServicoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrdemServicoForm {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200)
    private String titulo;

    @Size(max = 5000)
    private String descricao;

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(max = 150)
    private String clienteNome;

    @Size(max = 150)
    private String clienteEmail;

    @Size(max = 30)
    private String clienteTelefone;

    @NotNull(message = "Status é obrigatório")
    private OrdemServicoStatus status = OrdemServicoStatus.ABERTA;

    private BigDecimal valor;

    private LocalDate dataPrevista;

    private Long responsavelId;
}
