package com.rafaelkamp.flowpatterns.domain.solicitacao.command;

import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoCanceladaEvent;
import com.rafaelkamp.flowpatterns.domain.event.PublicadorEventos;
import com.rafaelkamp.flowpatterns.domain.historico.RegistradorHistorico;
import com.rafaelkamp.flowpatterns.domain.historico.TipoEventoHistorico;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.MaquinaEstadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.Transicao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

import java.time.Clock;
import java.util.Objects;

/**
 * Comando que cancela a solicitação a pedido do solicitante ou de um administrador.
 */
public class CancelarSolicitacaoCommand implements AcaoSolicitacao {

    private final Solicitacao solicitacao;
    private final Usuario ator;
    private final String motivo;
    private final MaquinaEstadosSolicitacao maquinaEstados;
    private final RegistradorHistorico registradorHistorico;
    private final PublicadorEventos publicadorEventos;
    private final Clock clock;

    public CancelarSolicitacaoCommand(Solicitacao solicitacao, Usuario ator, String motivo,
                                      MaquinaEstadosSolicitacao maquinaEstados,
                                      RegistradorHistorico registradorHistorico,
                                      PublicadorEventos publicadorEventos, Clock clock) {
        this.solicitacao = Objects.requireNonNull(solicitacao);
        this.ator = Objects.requireNonNull(ator);
        this.motivo = motivo;
        this.maquinaEstados = Objects.requireNonNull(maquinaEstados);
        this.registradorHistorico = Objects.requireNonNull(registradorHistorico);
        this.publicadorEventos = Objects.requireNonNull(publicadorEventos);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Long solicitacaoId() {
        return solicitacao.getId();
    }

    @Override
    public String descricao() {
        return "cancelar";
    }

    @Override
    public Transicao executar() {
        Transicao transicao = maquinaEstados.cancelar(solicitacao, ator, motivo);
        registradorHistorico.registrar(solicitacao, TipoEventoHistorico.CANCELAMENTO,
                transicao.statusAnterior(), transicao.statusNovo(), ator, motivo);
        publicadorEventos.publicar(new SolicitacaoCanceladaEvent(
                solicitacao.getId(),
                solicitacao.getSolicitante().getId(),
                ator.getId(),
                solicitacao.getTitulo(),
                motivo,
                clock.instant()));
        return transicao;
    }
}
