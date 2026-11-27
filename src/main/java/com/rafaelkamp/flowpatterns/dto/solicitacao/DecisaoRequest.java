package com.rafaelkamp.flowpatterns.dto.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Decisão de aprovação ou rejeição")
public record DecisaoRequest(

        @Schema(description = "Identificador do usuário que executa a decisão")
        @NotNull
        Long atorId,

        @Schema(description = "Observação registrada no histórico", example = "Aprovado conforme planejamento")
        @Size(max = 500)
        String observacao
) {
}
