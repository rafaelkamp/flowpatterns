package com.rafaelkamp.flowpatterns.dto.painel;

import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResumoResponse;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Indicadores consolidados da plataforma")
public record PainelResponse(
        long total,
        Map<StatusSolicitacao, Long> porStatus,
        Map<TipoSolicitacao, Long> porTipo,
        long emAndamento,
        long atrasadas,
        double tempoMedioAprovacaoHoras,
        List<SolicitacaoResumoResponse> ultimasSolicitacoes
) {
}
