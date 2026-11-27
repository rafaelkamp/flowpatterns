package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoAcao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Ação em lote aplicada a um conjunto de solicitações")
public record AcaoLoteRequest(

        @Schema(description = "Ação executada para cada solicitação")
        @NotNull
        TipoAcao acao,

        @Schema(description = "Identificadores das solicitações")
        @NotEmpty
        @Size(max = 100)
        List<Long> ids,

        @Schema(description = "Identificador do usuário que executa a ação")
        @NotNull
        Long atorId,

        @Schema(description = "Observação registrada para todas as solicitações")
        @Size(max = 500)
        String observacao
) {
}
