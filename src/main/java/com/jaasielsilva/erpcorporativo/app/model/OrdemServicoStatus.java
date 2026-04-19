package com.jaasielsilva.erpcorporativo.app.model;

/**
 * Ciclo de vida de uma Ordem de Serviço.
 * Projetado para escalar — novos status podem ser adicionados sem quebrar lógica existente.
 */
public enum OrdemServicoStatus {
    ABERTA,
    EM_ANDAMENTO,
    AGUARDANDO_CLIENTE,
    CONCLUIDA,
    CANCELADA
}
