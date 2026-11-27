package com.rafaelkamp.flowpatterns.domain.solicitacao.factory;

import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoBuilder;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoCompra;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class CompraFactory extends SolicitacaoFactory {

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.COMPRA;
    }

    @Override
    protected SolicitacaoBuilder<?> criarBuilder() {
        return new SolicitacaoCompra.Builder();
    }

    @Override
    protected void preencherDadosEspecificos(SolicitacaoBuilder<?> builder, DadosSolicitacao dados) {
        SolicitacaoCompra.Builder compra = (SolicitacaoCompra.Builder) builder;
        compra.valor(dados.valor())
                .fornecedor(dados.fornecedor())
                .quantidadeItens(dados.quantidadeItens());
    }
}
