package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

/**
 * Calendário de dias úteis com feriados nacionais fixos.
 * Feriados móveis (Carnaval, Páscoa, Corpus Christi) podem ser adicionados
 * futuramente por configuração sem impactar as estratégias de SLA.
 */
@Component
public class CalendarioUtil {

    private static final Set<MonthDay> FERIADOS_NACIONAIS = Set.of(
            MonthDay.of(1, 1),    // Confraternização Universal
            MonthDay.of(4, 21),   // Tiradentes
            MonthDay.of(5, 1),    // Dia do Trabalho
            MonthDay.of(9, 7),    // Independência
            MonthDay.of(10, 12),  // Nossa Senhora Aparecida
            MonthDay.of(11, 2),   // Finados
            MonthDay.of(11, 15),  // Proclamação da República
            MonthDay.of(11, 20),  // Consciência Negra
            MonthDay.of(12, 25)   // Natal
    );

    public LocalDate adicionarDiasUteis(LocalDate dataBase, int diasUteis) {
        LocalDate resultado = proximoDiaUtil(dataBase);
        int restantes = Math.max(0, diasUteis);
        while (restantes > 0) {
            resultado = resultado.plusDays(1);
            if (ehDiaUtil(resultado)) {
                restantes--;
            }
        }
        return resultado;
    }

    public LocalDate proximoDiaUtil(LocalDate data) {
        LocalDate resultado = data;
        while (!ehDiaUtil(resultado)) {
            resultado = resultado.plusDays(1);
        }
        return resultado;
    }

    public boolean ehDiaUtil(LocalDate data) {
        DayOfWeek diaSemana = data.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            return false;
        }
        return !FERIADOS_NACIONAIS.contains(MonthDay.from(data));
    }
}
