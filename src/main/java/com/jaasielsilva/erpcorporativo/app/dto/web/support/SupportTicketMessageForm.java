package com.jaasielsilva.erpcorporativo.app.dto.web.support;

import com.jaasielsilva.erpcorporativo.app.model.SupportMessageVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SupportTicketMessageForm {

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 5000, message = "Mensagem deve ter no máximo 5000 caracteres")
    private String conteudo;

    @NotNull(message = "Visibilidade é obrigatória")
    private SupportMessageVisibility visibilidade = SupportMessageVisibility.PUBLICO;
}
