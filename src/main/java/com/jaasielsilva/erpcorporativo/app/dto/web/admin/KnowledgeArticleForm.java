package com.jaasielsilva.erpcorporativo.app.dto.web.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import com.jaasielsilva.erpcorporativo.app.model.ArticleVisibility;

@Data
public class KnowledgeArticleForm {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200)
    private String titulo;

    @Size(max = 255)
    private String resumo;

    @NotBlank(message = "Conteúdo é obrigatório")
    private String conteudo;

    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 80)
    private String categoria;

    private ArticleVisibility visibilidade = ArticleVisibility.PUBLICO;

    private boolean publicado = true;

    /** Preenchido apenas para artigos PRIVADO */
    private Long tenantId;
}
