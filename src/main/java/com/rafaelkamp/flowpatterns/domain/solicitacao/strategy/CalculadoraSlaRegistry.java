package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Seleciona a Strategy de SLA pelo tipo e a decora com o ajuste de prioridade.
 */
@Component
public class CalculadoraSlaRegistry {

    private final Map<TipoSolicitacao, CalculadoraPrazoSla> calculadoras;

    public CalculadoraSlaRegistry(List<CalculadoraPrazoSla> estrategias) {
        this.calculadoras = new EnumMap<>(TipoSolicitacao.class);
        estrategias.forEach(estrategia ->
                this.calculadoras.put(estrategia.tipo(), new CalculadoraSlaComPrioridade(estrategia)));
    }

    public int calcularDiasUteis(Solicitacao solicitacao) {
        CalculadoraPrazoSla calculadora = calculadoras.get(solicitacao.getTipo());
        if (calculadora == null) {
            throw new RegraNegocioException("Não existe cálculo de SLA para o tipo " + solicitacao.getTipo());
        }
        return calculadora.calcularDiasUteis(solicitacao);
    }
}
