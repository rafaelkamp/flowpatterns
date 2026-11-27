package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Calcula o prazo final de SLA em dias úteis, sempre às 18h no fuso configurado.
 */
@Component
public class ServicoPrazoSla {

    private static final LocalTime HORARIO_LIMITE = LocalTime.of(18, 0);

    private final CalculadoraSlaRegistry calculadoraRegistry;
    private final CalendarioUtil calendario;
    private final Clock clock;

    public ServicoPrazoSla(CalculadoraSlaRegistry calculadoraRegistry, CalendarioUtil calendario, Clock clock) {
        this.calculadoraRegistry = calculadoraRegistry;
        this.calendario = calendario;
        this.clock = clock;
    }

    public Instant calcular(Solicitacao solicitacao, Instant referencia) {
        int diasUteis = calculadoraRegistry.calcularDiasUteis(solicitacao);
        LocalDate dataReferencia = LocalDate.ofInstant(referencia, clock.getZone());
        LocalDate dataPrazo = calendario.adicionarDiasUteis(dataReferencia, diasUteis);
        return dataPrazo.atTime(HORARIO_LIMITE).atZone(clock.getZone()).toInstant();
    }
}
