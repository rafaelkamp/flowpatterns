package com.rafaelkamp.flowpatterns.domain.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ação em lote que pode ser executada sobre solicitações")
public enum TipoAcao {
    APROVAR,
    REJEITAR,
    CANCELAR
}
