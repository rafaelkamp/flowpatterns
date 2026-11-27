package com.rafaelkamp.flowpatterns.domain.solicitacao;

import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;

/**
 * Utilitário que associa o tipo de negócio à classe concreta da entidade.
 */
public final class TiposSolicitacao {

    private TiposSolicitacao() {
    }

    public static Class<? extends Solicitacao> classeDe(TipoSolicitacao tipo) {
        if (tipo == null) {
            throw new RegraNegocioException("Tipo de solicitação não informado");
        }
        return switch (tipo) {
            case DESPESA -> SolicitacaoDespesa.class;
            case COMPRA -> SolicitacaoCompra.class;
            case FERIAS -> SolicitacaoFerias.class;
            case ACESSO -> SolicitacaoAcesso.class;
        };
    }
}
