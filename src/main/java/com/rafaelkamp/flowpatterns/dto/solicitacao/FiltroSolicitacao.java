package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Filtros de consulta de solicitações")
public record FiltroSolicitacao(
        StatusSolicitacao status,
        TipoSolicitacao tipo,
        Prioridade prioridade,
        Long solicitanteId,
        Long aprovadorId,
        String departamento,
        String texto,
        Boolean atrasada,
        Instant criadoDe,
        Instant criadoAte
) {
}
