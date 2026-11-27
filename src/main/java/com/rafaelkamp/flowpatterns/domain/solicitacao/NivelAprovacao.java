package com.rafaelkamp.flowpatterns.domain.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nível de alçada necessário para aprovar a solicitação")
public enum NivelAprovacao {
    GESTOR,
    DIRETOR
}
