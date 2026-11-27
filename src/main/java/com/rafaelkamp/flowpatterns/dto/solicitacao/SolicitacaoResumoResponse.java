package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resumo de uma solicitação, utilizado em listagens")
public record SolicitacaoResumoResponse(
        Long id,
        TipoSolicitacao tipo,
        String titulo,
        StatusSolicitacao status,
        Prioridade prioridade,
        String solicitanteNome,
        String aprovadorAtualNome,
        Instant prazoSla,
        boolean atrasada,
        Instant criadoEm
) {

    public static SolicitacaoResumoResponse from(Solicitacao solicitacao, Instant agora) {
        return new SolicitacaoResumoResponse(
                solicitacao.getId(),
                solicitacao.getTipo(),
                solicitacao.getTitulo(),
                solicitacao.getStatus(),
                solicitacao.getPrioridade(),
                solicitacao.getSolicitante().getNome(),
                solicitacao.getAprovadorAtual() == null ? null : solicitacao.getAprovadorAtual().getNome(),
                solicitacao.getPrazoSla(),
                solicitacao.estaAtrasada(agora),
                solicitacao.getCriadoEm()
        );
    }
}
