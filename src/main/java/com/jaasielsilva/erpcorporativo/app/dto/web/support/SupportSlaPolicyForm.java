package com.jaasielsilva.erpcorporativo.app.dto.web.support;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SupportSlaPolicyForm {

    @NotNull(message = "Tempo de primeira resposta é obrigatório")
    @Min(value = 5, message = "Tempo mínimo de primeira resposta é 5 minutos")
    @Max(value = 43200, message = "Tempo máximo de primeira resposta é 43200 minutos")
    private Integer firstResponseMinutes = 120;

    @NotNull(message = "Tempo de resolução é obrigatório")
    @Min(value = 10, message = "Tempo mínimo de resolução é 10 minutos")
    @Max(value = 86400, message = "Tempo máximo de resolução é 86400 minutos")
    private Integer resolutionMinutes = 1440;

    @NotNull(message = "Tempo de aviso é obrigatório")
    @Min(value = 1, message = "Aviso mínimo é 1 minuto")
    @Max(value = 10080, message = "Aviso máximo é 10080 minutos")
    private Integer warningBeforeMinutes = 120;
}
