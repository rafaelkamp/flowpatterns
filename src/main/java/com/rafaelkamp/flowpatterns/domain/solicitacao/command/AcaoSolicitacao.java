package com.rafaelkamp.flowpatterns.domain.solicitacao.command;

import com.rafaelkamp.flowpatterns.domain.solicitacao.state.Transicao;

/**
 * Comando do padrão <b>Command</b>: encapsula uma intenção de negócio sobre a
 * solicitação, permitindo execução individual ou em lote.
 */
public interface AcaoSolicitacao {

    Long solicitacaoId();

    String descricao();

    Transicao executar();
}
