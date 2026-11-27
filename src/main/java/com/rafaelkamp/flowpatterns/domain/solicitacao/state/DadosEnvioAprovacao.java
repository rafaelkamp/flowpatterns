package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

import java.time.Instant;

/**
 * Dados calculados pela cadeia de validação e aplicados no envio.
 */
public record DadosEnvioAprovacao(
        NivelAprovacao nivelFinal,
        NivelAprovacao nivelInicial,
        Usuario aprovadorInicial,
        Instant prazoSla
) {
}
