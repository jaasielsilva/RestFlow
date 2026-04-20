package com.jaasielsilva.erpcorporativo.app.dto.web.crm;

import com.jaasielsilva.erpcorporativo.app.model.TipoInteracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class InteracaoForm {

    @NotNull(message = "Tipo é obrigatório")
    private TipoInteracao tipo;

    @NotNull(message = "Data da interação é obrigatória")
    private LocalDateTime dataInteracao;

    @NotBlank(message = "Assunto é obrigatório")
    @Size(max = 200)
    private String assunto;

    private String descricao;

    private Long responsavelId;
}
