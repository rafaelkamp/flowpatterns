package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;

import java.util.Objects;

/**
 * Decorator que aplica o ajuste de prioridade sobre qualquer cálculo de SLA.
 *
 * <p>O cálculo base continua sendo responsabilidade da Strategy concreta; este
 * decorator apenas reduz o prazo para solicitações de alta prioridade, sem
 * alterar as classes existentes.</p>
 */
public class CalculadoraSlaComPrioridade implements CalculadoraPrazoSla {

    private final CalculadoraPrazoSla delegate;

    public CalculadoraSlaComPrioridade(CalculadoraPrazoSla delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public TipoSolicitacao tipo() {
        return delegate.tipo();
    }

    @Override
    public int calcularDiasUteis(Solicitacao solicitacao) {
        int prazoBase = delegate.calcularDiasUteis(solicitacao);
        int ajuste = switch (solicitacao.getPrioridade()) {
            case URGENTE -> 2;
            case ALTA -> 1;
            case MEDIA, BAIXA -> 0;
        };
        return Math.max(1, prazoBase - ajuste);
    }
}
