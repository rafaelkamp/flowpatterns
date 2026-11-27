package com.rafaelkamp.flowpatterns.domain.solicitacao.factory;

import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoBuilder;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

/**
 * Creator do padrão <b>Factory Method</b>.
 *
 * <p>Cada subtipo de solicitação possui seu próprio creator concreto, responsável
 * por instanciar o builder correto e preencher os campos específicos. O algoritmo
 * de criação é comum e fica nesta classe.</p>
 */
public abstract class SolicitacaoFactory {

    public Solicitacao criar(DadosSolicitacao dados, Usuario solicitante) {
        SolicitacaoBuilder<?> builder = criarBuilder();
        preencherDadosEspecificos(builder, dados);
        builder.dadosComuns(dados.titulo(), dados.descricao(), dados.prioridade(), solicitante);
        Solicitacao solicitacao = builder.build();
        solicitacao.validarDadosEspecificos();
        return solicitacao;
    }

    public abstract TipoSolicitacao tipo();

    protected abstract SolicitacaoBuilder<?> criarBuilder();

    protected abstract void preencherDadosEspecificos(SolicitacaoBuilder<?> builder, DadosSolicitacao dados);
}
