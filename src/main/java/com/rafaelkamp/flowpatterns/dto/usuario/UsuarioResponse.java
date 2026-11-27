package com.rafaelkamp.flowpatterns.dto.usuario;

import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Usuário retornado pela API")
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String departamento,
        PapelUsuario papel,
        boolean ativo,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
