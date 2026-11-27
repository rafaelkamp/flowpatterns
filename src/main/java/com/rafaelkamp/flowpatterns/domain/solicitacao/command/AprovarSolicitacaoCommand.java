package com.rafaelkamp.flowpatterns.domain.solicitacao.command;

import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoAprovadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.PublicadorEventos;
import com.rafaelkamp.flowpatterns.domain.historico.RegistradorHistorico;
import com.rafaelkamp.flowpatterns.domain.historico.TipoEventoHistorico;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.MaquinaEstadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.Transicao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

import java.time.Clock;
import java.util.Objects;

/**
 * Comando que aprova a solicitação, avançando ou concluindo o fluxo de alçada.
 */
public class AprovarSolicitacaoCommand implements AcaoSolicitacao {

    private final Solicitacao solicitacao;
    private final Usuario ator;
    private final String observacao;
    private final MaquinaEstadosSolicitacao maquinaEstados;
    private final RegistradorHistorico registradorHistorico;
    private final PublicadorEventos publicadorEventos;
    private final Clock clock;

    public AprovarSolicitacaoCommand(Solicitacao solicitacao, Usuario ator, String observacao,
                                     MaquinaEstadosSolicitacao maquinaEstados,
                                     RegistradorHistorico registradorHistorico,
                                     PublicadorEventos publicadorEventos, Clock clock) {
        this.solicitacao = Objects.requireNonNull(solicitacao);
        this.ator = Objects.requireNonNull(ator);
        this.observacao = observacao;
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
        return "aprovar";
    }

    @Override
    public Transicao executar() {
        Transicao transicao = maquinaEstados.aprovar(solicitacao, ator, observacao);
        registradorHistorico.registrar(solicitacao, TipoEventoHistorico.APROVACAO,
                transicao.statusAnterior(), transicao.statusNovo(), ator, observacao);
        publicadorEventos.publicar(new SolicitacaoAprovadaEvent(
                solicitacao.getId(),
                solicitacao.getSolicitante().getId(),
                ator.getId(),
                solicitacao.getTitulo(),
                solicitacao.getStatus().name(),
                proximoAprovadorId(),
                clock.instant()));
        return transicao;
    }

    private Long proximoAprovadorId() {
        if (solicitacao.getStatus() == StatusSolicitacao.EM_ANALISE && solicitacao.getAprovadorAtual() != null) {
            return solicitacao.getAprovadorAtual().getId();
        }
        return null;
    }
}
