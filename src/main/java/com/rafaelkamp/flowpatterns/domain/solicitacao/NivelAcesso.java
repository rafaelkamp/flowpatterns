package com.rafaelkamp.flowpatterns.domain.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nível de acesso solicitado em uma solicitação do tipo ACESSO")
public enum NivelAcesso {
    BASICO,
    INTERMEDIARIO,
    ADMINISTRATIVO
}
