package com.rafaelkamp.flowpatterns.domain.solicitacao;

import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

import java.util.Objects;

/**
 * Builder base para a construção de solicitações.
 *
 * <p>O padrão Builder foi aplicado porque as solicitações possuem muitos campos,
 * sendo parte comum a todos os tipos e parte específica de cada subtipo. Isso
 * evita construtores telescópicos e mantém a criação legível e segura.</p>
 *
 * @param <T> tipo concreto de solicitação construída
 */
public abstract class SolicitacaoBuilder<T extends Solicitacao> {

    protected String titulo;
    protected String descricao;
    protected Prioridade prioridade;
    protected Usuario solicitante;

    public SolicitacaoBuilder<T> dadosComuns(String titulo, String descricao,
                                             Prioridade prioridade, Usuario solicitante) {
        this.titulo = Objects.requireNonNull(titulo, "titulo").trim();
        this.descricao = Objects.requireNonNull(descricao, "descricao").trim();
        this.prioridade = Objects.requireNonNull(prioridade, "prioridade");
        this.solicitante = Objects.requireNonNull(solicitante, "solicitante");
        return this;
    }

    public abstract T build();
}
