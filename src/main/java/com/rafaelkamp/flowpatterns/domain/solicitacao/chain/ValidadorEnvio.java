package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

/**
 * Handler base do padrão <b>Chain of Responsibility</b>.
 *
 * <p>Cada validação decide se a solicitação pode prosseguir. O próximo handler
 * só é executado se o atual concluir sem exceção.</p>
 */
public abstract class ValidadorEnvio {

    private ValidadorEnvio proximo;

    public ValidadorEnvio definirProximo(ValidadorEnvio proximo) {
        this.proximo = proximo;
        return this;
    }

    public void validar(ContextoEnvio contexto) {
        validarInterno(contexto);
        if (proximo != null) {
            proximo.validar(contexto);
        }
    }

    protected abstract void validarInterno(ContextoEnvio contexto);
}
