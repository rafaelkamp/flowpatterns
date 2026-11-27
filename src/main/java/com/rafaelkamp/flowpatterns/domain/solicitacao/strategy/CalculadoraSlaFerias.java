package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraSlaFerias implements CalculadoraPrazoSla {

    private static final int PRAZO_BASE_DIAS_UTEIS = 7;

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.FERIAS;
    }

    @Override
    public int calcularDiasUteis(Solicitacao solicitacao) {
        return PRAZO_BASE_DIAS_UTEIS;
    }
}
