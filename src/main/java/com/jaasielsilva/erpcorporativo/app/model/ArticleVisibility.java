package com.jaasielsilva.erpcorporativo.app.model;

/**
 * PUBLICO   — visível para todos os tenants autenticados
 * PRIVADO   — visível apenas para o tenant específico vinculado ao artigo
 * INTERNO   — visível apenas para o SUPER_ADMIN (rascunhos internos)
 */
public enum ArticleVisibility {
    PUBLICO,
    PRIVADO,
    INTERNO
}
