package com.jaasielsilva.erpcorporativo.app.dto.web.support;

import com.jaasielsilva.erpcorporativo.app.model.SupportTicketPriority;
import com.jaasielsilva.erpcorporativo.app.model.SupportTicketStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SupportTicketForm {

    @NotBlank(message = "Assunto é obrigatório")
    @Size(max = 200, message = "Assunto deve ter no máximo 200 caracteres")
    private String assunto;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    private String descricao;

    @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
    private String categoria;

    @NotNull(message = "Status é obrigatório")
    private SupportTicketStatus status = SupportTicketStatus.ABERTO;

    @NotNull(message = "Prioridade é obrigatória")
    private SupportTicketPriority prioridade = SupportTicketPriority.MEDIA;

    private Long clienteId;

    private Long responsavelId;

    @Size(max = 150, message = "Nome do solicitante deve ter no máximo 150 caracteres")
    private String solicitanteNome;

    @Email(message = "Email do solicitante inválido")
    @Size(max = 150, message = "Email do solicitante deve ter no máximo 150 caracteres")
    private String solicitanteEmail;
}
