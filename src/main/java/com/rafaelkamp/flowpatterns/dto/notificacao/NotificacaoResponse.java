package com.rafaelkamp.flowpatterns.dto.notificacao;

import com.rafaelkamp.flowpatterns.domain.notificacao.Notificacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Notificação interna recebida por um usuário")
public record NotificacaoResponse(
        Long id,
        String titulo,
        String mensagem,
        boolean lida,
        Long solicitacaoId,
        Instant criadaEm,
        Instant lidaEm
) {

    public static NotificacaoResponse from(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.isLida(),
                notificacao.getSolicitacao() == null ? null : notificacao.getSolicitacao().getId(),
                notificacao.getCriadaEm(),
                notificacao.getLidaEm()
        );
    }
}
