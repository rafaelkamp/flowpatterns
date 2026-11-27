package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoCompra;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Strategy e Decorator - cálculo de SLA")
class CalculadoraSlaTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-03-10T12:00:00Z"), ZoneOffset.UTC);

    private final Usuario solicitante = new Usuario("Ana", "ana@teste.local", "Financeiro", PapelUsuario.SOLICITANTE);

    private CalculadoraSlaRegistry registry() {
        return new CalculadoraSlaRegistry(List.of(
                new CalculadoraSlaDespesa(), new CalculadoraSlaCompra(),
                new CalculadoraSlaFerias(), new CalculadoraSlaAcesso()));
    }

    @Test
    @DisplayName("cada tipo de solicitação possui seu prazo base")
    void prazoBasePorTipo() {
        CalculadoraSlaRegistry registry = registry();

        assertThat(registry.calcularDiasUteis(despesa(Prioridade.MEDIA))).isEqualTo(3);
        assertThat(registry.calcularDiasUteis(compra(Prioridade.MEDIA))).isEqualTo(5);
        assertThat(registry.calcularDiasUteis(acesso(Prioridade.MEDIA))).isEqualTo(2);
    }

    @Test
    @DisplayName("decorator de prioridade reduz o prazo das solicitações urgentes")
    void decoratorReduzPrazoPorPrioridade() {
        CalculadoraSlaRegistry registry = registry();

        assertThat(registry.calcularDiasUteis(despesa(Prioridade.ALTA))).isEqualTo(2);
        assertThat(registry.calcularDiasUteis(despesa(Prioridade.URGENTE))).isEqualTo(1);
        assertThat(registry.calcularDiasUteis(despesa(Prioridade.BAIXA))).isEqualTo(3);
    }

    @Test
    @DisplayName("decorator preserva o comportamento da strategy encapsulada")
    void decoratorDelegaParaStrategy() {
        CalculadoraPrazoSla base = new CalculadoraSlaCompra();
        CalculadoraPrazoSla decorada = new CalculadoraSlaComPrioridade(base);

        assertThat(decorada.tipo()).isEqualTo(base.tipo());
        assertThat(decorada.calcularDiasUteis(compra(Prioridade.MEDIA))).isEqualTo(5);
    }

    @Test
    @DisplayName("prazo final é sempre um dia útil às 18h")
    void calculaPrazoEmDiaUtil() {
        ServicoPrazoSla servico = new ServicoPrazoSla(registry(), new CalendarioUtil(),
                Clock.fixed(Instant.parse("2026-03-10T12:00:00Z"), ZoneId.of("America/Sao_Paulo")));

        Instant prazo = servico.calcular(despesa(Prioridade.MEDIA), Instant.parse("2026-03-10T12:00:00Z"));

        assertThat(prazo).isEqualTo(Instant.parse("2026-03-13T21:00:00Z"));
    }

    @Test
    @DisplayName("calendário ignora fins de semana e feriados nacionais")
    void calendarioConsideraFeriados() {
        CalendarioUtil calendario = new CalendarioUtil();

        assertThat(calendario.ehDiaUtil(LocalDate.of(2026, 11, 20))).isFalse();
        assertThat(calendario.ehDiaUtil(LocalDate.of(2026, 11, 21))).isFalse();
        assertThat(calendario.ehDiaUtil(LocalDate.of(2026, 11, 23))).isTrue();
        assertThat(calendario.adicionarDiasUteis(LocalDate.of(2026, 11, 20), 1))
                .isEqualTo(LocalDate.of(2026, 11, 24));
    }

    private Solicitacao despesa(Prioridade prioridade) {
        return new SolicitacaoDespesa.Builder()
                .valor(new BigDecimal("100.00"))
                .categoriaDespesa(CategoriaDespesa.MATERIAL)
                .dadosComuns("Despesa", "Descrição", prioridade, solicitante)
                .build();
    }

    private Solicitacao compra(Prioridade prioridade) {
        return new SolicitacaoCompra.Builder()
                .valor(new BigDecimal("100.00"))
                .fornecedor("Fornecedor")
                .quantidadeItens(2)
                .dadosComuns("Compra", "Descrição", prioridade, solicitante)
                .build();
    }

    private Solicitacao acesso(Prioridade prioridade) {
        return new SolicitacaoAcesso.Builder()
                .sistema("ERP")
                .nivelAcesso(NivelAcesso.BASICO)
                .dadosComuns("Acesso", "Descrição", prioridade, solicitante)
                .build();
    }
}
