package com.rafaelkamp.flowpatterns.domain.solicitacao.factory;

import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoBuilder;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class DespesaFactory extends SolicitacaoFactory {

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.DESPESA;
    }

    @Override
    protected SolicitacaoBuilder<?> criarBuilder() {
        return new SolicitacaoDespesa.Builder();
    }

    @Override
    protected void preencherDadosEspecificos(SolicitacaoBuilder<?> builder, DadosSolicitacao dados) {
        SolicitacaoDespesa.Builder despesa = (SolicitacaoDespesa.Builder) builder;
        despesa.valor(dados.valor())
                .categoriaDespesa(dados.categoriaDespesa())
                .centroCusto(dados.centroCusto());
    }
}
