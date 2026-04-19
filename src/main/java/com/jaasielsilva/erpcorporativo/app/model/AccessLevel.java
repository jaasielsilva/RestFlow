package com.jaasielsilva.erpcorporativo.app.model;

/**
 * Nível de acesso de uma role a um módulo dentro de um tenant.
 *
 * NONE     — sem acesso (módulo não aparece para a role)
 * READ     — somente visualização
 * WRITE    — visualização + criação/edição
 * FULL     — acesso completo incluindo exclusão e configurações
 */
public enum AccessLevel {
    NONE,
    READ,
    WRITE,
    FULL
}
