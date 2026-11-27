package com.rafaelkamp.flowpatterns.domain.solicitacao.factory;

import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoBuilder;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class AcessoFactory extends SolicitacaoFactory {

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.ACESSO;
    }

    @Override
    protected SolicitacaoBuilder<?> criarBuilder() {
        return new SolicitacaoAcesso.Builder();
    }

    @Override
    protected void preencherDadosEspecificos(SolicitacaoBuilder<?> builder, DadosSolicitacao dados) {
        SolicitacaoAcesso.Builder acesso = (SolicitacaoAcesso.Builder) builder;
        acesso.sistema(dados.sistema())
                .nivelAcesso(dados.nivelAcesso());
    }
}
