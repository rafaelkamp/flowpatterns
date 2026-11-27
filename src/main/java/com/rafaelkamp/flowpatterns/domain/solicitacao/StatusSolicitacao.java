package com.rafaelkamp.flowpatterns.domain.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status do ciclo de vida da solicitação")
public enum StatusSolicitacao {
    RASCUNHO,
    PENDENTE,
    EM_ANALISE,
    APROVADA,
    REJEITADA,
    CANCELADA;

    public boolean emAndamento() {
        return this == PENDENTE || this == EM_ANALISE;
    }

    public boolean finalizado() {
        return this == APROVADA || this == REJEITADA || this == CANCELADA;
    }
}
