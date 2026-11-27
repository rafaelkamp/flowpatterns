package com.rafaelkamp.flowpatterns.domain.solicitacao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria de uma despesa")
public enum CategoriaDespesa {
    VIAGEM,
    MATERIAL,
    SERVICO,
    TREINAMENTO,
    OUTROS
}
