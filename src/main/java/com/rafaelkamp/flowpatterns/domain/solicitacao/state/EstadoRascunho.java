package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.SeletorAprovador;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class EstadoRascunho extends EstadoSolicitacaoBase {

    public EstadoRascunho(Clock clock, SeletorAprovador seletorAprovador) {
        super(clock, seletorAprovador);
    }

    @Override
    public StatusSolicitacao status() {
        return StatusSolicitacao.RASCUNHO;
    }

    @Override
    public Transicao enviar(Solicitacao solicitacao, DadosEnvioAprovacao dados) {
        solicitacao.enviarParaAprovacao(dados.nivelFinal(), dados.nivelInicial(),
                dados.aprovadorInicial(), dados.prazoSla(), agora());
        return new Transicao(StatusSolicitacao.RASCUNHO, StatusSolicitacao.PENDENTE,
                "Solicitação enviada para aprovação");
    }

    @Override
    public Transicao aprovar(Solicitacao solicitacao, Usuario ator, String observacao) {
        throw new TransicaoInvalidaException("Rascunhos não podem ser aprovados");
    }

    @Override
    public Transicao rejeitar(Solicitacao solicitacao, Usuario ator, String observacao) {
        throw new TransicaoInvalidaException("Rascunhos não podem ser rejeitados");
    }

    @Override
    public Transicao cancelar(Solicitacao solicitacao, Usuario ator, String motivo) {
        validarSolicitanteOuAdministrador(solicitacao, ator);
        solicitacao.cancelar(motivo, agora());
        return new Transicao(StatusSolicitacao.RASCUNHO, StatusSolicitacao.CANCELADA,
                motivo == null ? "Solicitação cancelada" : motivo);
    }

    @Override
    public void validarEdicao(Solicitacao solicitacao) {
        // Rascunhos podem ser editados.
    }
}
