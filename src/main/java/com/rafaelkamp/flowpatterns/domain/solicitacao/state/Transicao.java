package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;

/**
 * Resultado de uma transição de estado.
 */
public record Transicao(
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        String detalhe
) {
}
