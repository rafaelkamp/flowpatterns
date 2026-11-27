package com.rafaelkamp.flowpatterns.dto.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitação de cancelamento")
public record CancelamentoRequest(

        @Schema(description = "Identificador do solicitante ou de um administrador")
        @NotNull
        Long atorId,

        @Schema(description = "Motivo do cancelamento")
        @NotBlank
        @Size(max = 500)
        String motivo
) {
}
