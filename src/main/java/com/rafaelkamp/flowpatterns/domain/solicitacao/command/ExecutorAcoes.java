package com.rafaelkamp.flowpatterns.domain.solicitacao.command;

import com.rafaelkamp.flowpatterns.domain.solicitacao.state.Transicao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Invoker do padrão <b>Command</b>: executa comandos sem conhecer o tipo
 * concreto da ação, permitindo ações unitárias ou em lote.
 */
@Component
public class ExecutorAcoes {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorAcoes.class);

    public Transicao executar(AcaoSolicitacao acao) {
        LOG.info("Executando ação '{}' na solicitação {}", acao.descricao(), acao.solicitacaoId());
        Transicao transicao = acao.executar();
        LOG.info("Ação '{}' concluída: {} -> {}",
                acao.descricao(), transicao.statusAnterior(), transicao.statusNovo());
        return transicao;
    }
}
