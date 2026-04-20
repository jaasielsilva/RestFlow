package com.jaasielsilva.erpcorporativo.app.dto.web.publicsite;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PublicSubscriptionForm {

    @NotNull(message = "Selecione um plano válido.")
    private Long planId;

    @NotBlank(message = "Nome da empresa é obrigatório.")
    @Size(max = 120, message = "Nome da empresa deve ter no máximo 120 caracteres.")
    private String tenantNome;

    @NotBlank(message = "Slug é obrigatório.")
    @Size(max = 80, message = "Slug deve ter no máximo 80 caracteres.")
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug deve conter apenas letras minúsculas, números e hífen."
    )
    private String tenantSlug;

    @NotBlank(message = "Nome do administrador é obrigatório.")
    @Size(max = 150, message = "Nome do administrador deve ter no máximo 150 caracteres.")
    private String adminNome;

    @NotBlank(message = "E-mail do administrador é obrigatório.")
    @Email(message = "E-mail do administrador inválido.")
    @Size(max = 150, message = "E-mail do administrador deve ter no máximo 150 caracteres.")
    private String adminEmail;

    // Honeypot anti-bot (deve permanecer vazio)
    @Size(max = 1, message = "Campo inválido.")
    private String website;

    @AssertTrue(message = "É necessário aceitar os termos para continuar.")
    private boolean aceiteTermos;
}
