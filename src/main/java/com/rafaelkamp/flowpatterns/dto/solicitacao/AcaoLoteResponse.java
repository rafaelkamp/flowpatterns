package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoAcao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado da execução de uma ação em lote")
public record AcaoLoteResponse(
        TipoAcao acao,
        int total,
        long sucesso,
        long falha,
        List<ItemAcao> resultados
) {

    @Schema(description = "Resultado individual de uma solicitação")
    public record ItemAcao(
            Long solicitacaoId,
            boolean sucesso,
            String mensagem,
            String status
    ) {
    }
}
