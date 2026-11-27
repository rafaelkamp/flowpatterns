package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.historico.HistoricoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.historico.TipoEventoHistorico;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Registro do histórico de auditoria da solicitação")
public record HistoricoResponse(
        Long id,
        TipoEventoHistorico tipoEvento,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        Long atorId,
        String atorNome,
        String detalhe,
        Instant ocorridoEm
) {

    public static HistoricoResponse from(HistoricoSolicitacao historico) {
        return new HistoricoResponse(
                historico.getId(),
                historico.getTipoEvento(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getAtor() == null ? null : historico.getAtor().getId(),
                historico.getAtor() == null ? null : historico.getAtor().getNome(),
                historico.getDetalhe(),
                historico.getOcorridoEm()
        );
    }
}
