package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

/**
 * Contexto compartilhado pelos handlers da cadeia de envio.
 */
public class ContextoEnvio {

    private final Solicitacao solicitacao;
    private NivelAprovacao nivelFinal;
    private NivelAprovacao nivelInicial;
    private Usuario aprovadorInicial;

    public ContextoEnvio(Solicitacao solicitacao) {
        this.solicitacao = solicitacao;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public NivelAprovacao getNivelFinal() {
        return nivelFinal;
    }

    public void setNivelFinal(NivelAprovacao nivelFinal) {
        this.nivelFinal = nivelFinal;
    }

    public NivelAprovacao getNivelInicial() {
        return nivelInicial;
    }

    public void setNivelInicial(NivelAprovacao nivelInicial) {
        this.nivelInicial = nivelInicial;
    }

    public Usuario getAprovadorInicial() {
        return aprovadorInicial;
    }

    public void setAprovadorInicial(Usuario aprovadorInicial) {
        this.aprovadorInicial = aprovadorInicial;
    }
}
