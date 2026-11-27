package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;

/**
 * Strategy que define a alçada necessária para aprovar uma solicitação.
 */
public interface PoliticaAlcadaAprovacao {

    TipoSolicitacao tipo();

    NivelAprovacao definirNivel(Solicitacao solicitacao);
}
