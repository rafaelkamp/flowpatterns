package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.SeletorAprovador;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;

import java.time.Clock;

/**
 * Base dos estados finais (aprovada, rejeitada e cancelada), nos quais
 * nenhuma transição adicional é permitida.
 */
public abstract class EstadoFinal extends EstadoSolicitacaoBase {

    protected EstadoFinal(Clock clock, SeletorAprovador seletorAprovador) {
        super(clock, seletorAprovador);
    }

    @Override
    public Transicao enviar(Solicitacao solicitacao, DadosEnvioAprovacao dados) {
        throw new TransicaoInvalidaException("Solicitações finalizadas não podem ser enviadas");
    }

    @Override
    public Transicao aprovar(Solicitacao solicitacao, Usuario ator, String observacao) {
        throw new TransicaoInvalidaException("Solicitações finalizadas não podem ser aprovadas");
    }

    @Override
    public Transicao rejeitar(Solicitacao solicitacao, Usuario ator, String observacao) {
        throw new TransicaoInvalidaException("Solicitações finalizadas não podem ser rejeitadas");
    }

    @Override
    public Transicao cancelar(Solicitacao solicitacao, Usuario ator, String motivo) {
        throw new TransicaoInvalidaException("Solicitações finalizadas não podem ser canceladas");
    }

    @Override
    public void validarEdicao(Solicitacao solicitacao) {
        throw new TransicaoInvalidaException("Somente solicitações em rascunho podem ser editadas");
    }
}
