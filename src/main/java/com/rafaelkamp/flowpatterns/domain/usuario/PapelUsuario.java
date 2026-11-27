package com.rafaelkamp.flowpatterns.domain.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Papel do usuário na plataforma")
public enum PapelUsuario {
    SOLICITANTE,
    GESTOR,
    DIRETOR,
    ADMINISTRADOR
}
