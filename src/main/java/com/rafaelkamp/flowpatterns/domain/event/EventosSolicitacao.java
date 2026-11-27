package com.rafaelkamp.flowpatterns.domain.event;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;

import java.time.Instant;

/**
 * Eventos de domínio publicados quando o ciclo de vida da solicitação muda.
 * O Spring aplica o padrão <b>Observer</b> para propagar esses eventos aos
 * ouvintes interested (notificações, auditoria, integrações).
 */
public final class EventosSolicitacao {

    private EventosSolicitacao() {
    }

    public record SolicitacaoCriadaEvent(
            Long solicitacaoId,
            Long solicitanteId,
            String titulo,
            Instant ocorridoEm
    ) {
    }

    public record SolicitacaoEnviadaEvent(
            Long solicitacaoId,
            Long solicitanteId,
            Long aprovadorId,
            String titulo,
            NivelAprovacao nivelAprovacao,
            Instant prazoSla,
            Instant ocorridoEm
    ) {
    }

    public record SolicitacaoAprovadaEvent(
            Long solicitacaoId,
            Long solicitanteId,
            Long aprovadorId,
            String titulo,
            String statusNovo,
            Long proximoAprovadorId,
            Instant ocorridoEm
    ) {
    }

    public record SolicitacaoRejeitadaEvent(
            Long solicitacaoId,
            Long solicitanteId,
            Long aprovadorId,
            String titulo,
            String motivo,
            Instant ocorridoEm
    ) {
    }

    public record SolicitacaoCanceladaEvent(
            Long solicitacaoId,
            Long solicitanteId,
            Long atorId,
            String titulo,
            String motivo,
            Instant ocorridoEm
    ) {
    }
}
