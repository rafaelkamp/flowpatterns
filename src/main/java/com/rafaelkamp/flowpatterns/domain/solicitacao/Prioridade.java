package com.rafaelkamp.flowpatterns.domain.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Prioridade da solicitação; impacta o prazo de SLA")
public enum Prioridade {
    BAIXA,
    MEDIA,
    ALTA,
    URGENTE
}
