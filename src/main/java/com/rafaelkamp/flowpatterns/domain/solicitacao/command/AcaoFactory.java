package com.rafaelkamp.flowpatterns.domain.solicitacao.command;

import com.rafaelkamp.flowpatterns.domain.event.PublicadorEventos;
import com.rafaelkamp.flowpatterns.domain.historico.RegistradorHistorico;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.MaquinaEstadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Objects;

/**
 * Factory de comandos: centraliza a criação dos comandos concretos com
 * suas dependências, separando a criação da execução.
 */
@Component
public class AcaoFactory {

    private final MaquinaEstadosSolicitacao maquinaEstados;
    private final RegistradorHistorico registradorHistorico;
    private final PublicadorEventos publicadorEventos;
    private final Clock clock;

    public AcaoFactory(MaquinaEstadosSolicitacao maquinaEstados,
                       RegistradorHistorico registradorHistorico,
                       PublicadorEventos publicadorEventos, Clock clock) {
        this.maquinaEstados = Objects.requireNonNull(maquinaEstados);
        this.registradorHistorico = Objects.requireNonNull(registradorHistorico);
        this.publicadorEventos = Objects.requireNonNull(publicadorEventos);
        this.clock = Objects.requireNonNull(clock);
    }

    public AcaoSolicitacao aprovar(Solicitacao solicitacao, Usuario ator, String observacao) {
        return new AprovarSolicitacaoCommand(solicitacao, ator, observacao,
                maquinaEstados, registradorHistorico, publicadorEventos, clock);
    }

    public AcaoSolicitacao rejeitar(Solicitacao solicitacao, Usuario ator, String observacao) {
        return new RejeitarSolicitacaoCommand(solicitacao, ator, observacao,
                maquinaEstados, registradorHistorico, publicadorEventos, clock);
    }

    public AcaoSolicitacao cancelar(Solicitacao solicitacao, Usuario ator, String motivo) {
        return new CancelarSolicitacaoCommand(solicitacao, ator, motivo,
                maquinaEstados, registradorHistorico, publicadorEventos, clock);
    }
}
