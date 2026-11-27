package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.exception.AcessoNegadoException;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorStatusRascunho extends ValidadorEnvio {

    @Override
    protected void validarInterno(ContextoEnvio contexto) {
        Solicitacao solicitacao = contexto.getSolicitacao();
        if (solicitacao.getStatus() != StatusSolicitacao.RASCUNHO) {
            throw new TransicaoInvalidaException(
                    "Somente solicitações em rascunho podem ser enviadas para aprovação");
        }
        if (!solicitacao.getSolicitante().isAtivo()) {
            throw new AcessoNegadoException("O solicitante está inativo e não pode enviar solicitações");
        }
    }
}
