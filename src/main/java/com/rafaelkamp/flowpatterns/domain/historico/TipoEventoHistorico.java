package com.rafaelkamp.flowpatterns.domain.historico;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de evento registrado no histórico da solicitação")
public enum TipoEventoHistorico {
    CRIACAO,
    ATUALIZACAO,
    ENVIO,
    APROVACAO,
    REJEICAO,
    CANCELAMENTO
}
