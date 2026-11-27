package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraSlaAcesso implements CalculadoraPrazoSla {

    private static final int PRAZO_BASE_DIAS_UTEIS = 2;

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.ACESSO;
    }

    @Override
    public int calcularDiasUteis(Solicitacao solicitacao) {
        return PRAZO_BASE_DIAS_UTEIS;
    }
}
