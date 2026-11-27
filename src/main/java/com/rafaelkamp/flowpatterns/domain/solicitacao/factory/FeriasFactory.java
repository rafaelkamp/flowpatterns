package com.rafaelkamp.flowpatterns.domain.solicitacao.factory;

import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoBuilder;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoFerias;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class FeriasFactory extends SolicitacaoFactory {

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.FERIAS;
    }

    @Override
    protected SolicitacaoBuilder<?> criarBuilder() {
        return new SolicitacaoFerias.Builder();
    }

    @Override
    protected void preencherDadosEspecificos(SolicitacaoBuilder<?> builder, DadosSolicitacao dados) {
        SolicitacaoFerias.Builder ferias = (SolicitacaoFerias.Builder) builder;
        ferias.dataInicio(dados.dataInicio())
                .dataFim(dados.dataFim());
    }
}
