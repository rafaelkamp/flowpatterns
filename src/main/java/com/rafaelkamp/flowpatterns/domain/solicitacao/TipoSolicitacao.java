package com.rafaelkamp.flowpatterns.domain.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de solicitação suportado pela plataforma")
public enum TipoSolicitacao {
    DESPESA,
    COMPRA,
    FERIAS,
    ACESSO
}
