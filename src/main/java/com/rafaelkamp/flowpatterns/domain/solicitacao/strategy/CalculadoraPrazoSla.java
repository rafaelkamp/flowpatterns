package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;

/**
 * Strategy de cálculo de SLA. Cada tipo de solicitação possui uma política
 * própria de prazo, permitindo evolução independente e composição com decorators.
 */
public interface CalculadoraPrazoSla {

    TipoSolicitacao tipo();

    int calcularDiasUteis(Solicitacao solicitacao);
}
