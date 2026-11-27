package com.rafaelkamp.flowpatterns.domain.solicitacao.command;

import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoAprovadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoCanceladaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoRejeitadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.PublicadorEventos;
import com.rafaelkamp.flowpatterns.domain.historico.RegistradorHistorico;
import com.rafaelkamp.flowpatterns.domain.historico.TipoEventoHistorico;
import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.MaquinaEstadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.Transicao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.AcessoNegadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Command - ações sobre solicitações")
class ComandoSolicitacaoTest {

    private static final Instant AGORA = Instant.parse("2026-03-10T12:00:00Z");
    private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

    @Mock
    private MaquinaEstadosSolicitacao maquinaEstados;

    @Mock
    private RegistradorHistorico registradorHistorico;

    @Mock
    private PublicadorEventos publicadorEventos;

    private Solicitacao solicitacao;
    private Usuario gestor;
    private Usuario solicitante;

    @BeforeEach
    void setUp() {
        solicitante = new Usuario("Ana", "ana@teste.local", "Financeiro", PapelUsuario.SOLICITANTE);
        solicitante.setId(1L);
        gestor = new Usuario("Gustavo", "gustavo@teste.local", "Financeiro", PapelUsuario.GESTOR);
        gestor.setId(2L);

        solicitacao = new SolicitacaoDespesa.Builder()
                .valor(new BigDecimal("100.00"))
                .categoriaDespesa(CategoriaDespesa.MATERIAL)
                .dadosComuns("Material", "Descrição", Prioridade.MEDIA, solicitante)
                .build();
        solicitacao.enviarParaAprovacao(NivelAprovacao.GESTOR, NivelAprovacao.GESTOR, gestor,
                AGORA.plusSeconds(86400), AGORA);
    }

    @Test
    @DisplayName("comando de aprovação registra histórico e publica evento")
    void comandoAprovarRegistraEPublica() {
        when(maquinaEstados.aprovar(solicitacao, gestor, "Aprovado"))
                .thenReturn(new Transicao(StatusSolicitacao.PENDENTE, StatusSolicitacao.APROVADA, "Solicitação aprovada"));

        AcaoSolicitacao acao = new AprovarSolicitacaoCommand(solicitacao, gestor, "Aprovado",
                maquinaEstados, registradorHistorico, publicadorEventos, RELOGIO);
        Transicao transicao = acao.executar();

        assertThat(transicao.statusNovo()).isEqualTo(StatusSolicitacao.APROVADA);
        verify(registradorHistorico).registrar(solicitacao, TipoEventoHistorico.APROVACAO,
                StatusSolicitacao.PENDENTE, StatusSolicitacao.APROVADA, gestor, "Aprovado");

        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(publicadorEventos).publicar(evento.capture());
        assertThat(evento.getValue()).isInstanceOf(SolicitacaoAprovadaEvent.class);
        SolicitationAsserts.check((SolicitacaoAprovadaEvent) evento.getValue(), solicitacao, gestor);
    }

    @Test
    @DisplayName("comando de aprovação identifica o próximo aprovador na segunda etapa")
    void comandoAprovarIdentificaProximoAprovador() {
        Usuario diretor = new Usuario("Helena", "helena@teste.local", "Diretoria", PapelUsuario.DIRETOR);
        diretor.setId(3L);
        solicitacao.avancarNivel(NivelAprovacao.DIRETOR, diretor);
        when(maquinaEstados.aprovar(solicitacao, diretor, "Aprovado"))
                .thenReturn(new Transicao(StatusSolicitacao.EM_ANALISE, StatusSolicitacao.APROVADA, "Solicitação aprovada"));

        AcaoSolicitacao acao = new AprovarSolicitacaoCommand(solicitacao, diretor, "Aprovado",
                maquinaEstados, registradorHistorico, publicadorEventos, RELOGIO);
        acao.executar();

        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(publicadorEventos).publicar(evento.capture());
        SolicitationAsserts.check((SolicitacaoAprovadaEvent) evento.getValue(), solicitacao, diretor);
    }

    @Test
    @DisplayName("comando de rejeição publica evento de rejeição")
    void comandoRejeitarPublicaEvento() {
        when(maquinaEstados.rejeitar(solicitacao, gestor, "Sem orçamento"))
                .thenReturn(new Transicao(StatusSolicitacao.PENDENTE, StatusSolicitacao.REJEITADA, "Sem orçamento"));

        AcaoSolicitacao acao = new RejeitarSolicitacaoCommand(solicitacao, gestor, "Sem orçamento",
                maquinaEstados, registradorHistorico, publicadorEventos, RELOGIO);
        acao.executar();

        verify(registradorHistorico).registrar(solicitacao, TipoEventoHistorico.REJEICAO,
                StatusSolicitacao.PENDENTE, StatusSolicitacao.REJEITADA, gestor, "Sem orçamento");
        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(publicadorEventos).publicar(evento.capture());
        assertThat(evento.getValue()).isInstanceOf(SolicitacaoRejeitadaEvent.class);
    }

    @Test
    @DisplayName("comando de cancelamento registra histórico e publica evento")
    void comandoCancelarPublicaEvento() {
        when(maquinaEstados.cancelar(solicitacao, solicitante, "Pedido equivocado"))
                .thenReturn(new Transicao(StatusSolicitacao.PENDENTE, StatusSolicitacao.CANCELADA, "Pedido equivocado"));

        AcaoSolicitacao acao = new CancelarSolicitacaoCommand(solicitacao, solicitante, "Pedido equivocado",
                maquinaEstados, registradorHistorico, publicadorEventos, RELOGIO);
        acao.executar();

        verify(registradorHistorico).registrar(solicitacao, TipoEventoHistorico.CANCELAMENTO,
                StatusSolicitacao.PENDENTE, StatusSolicitacao.CANCELADA, solicitante, "Pedido equivocado");
        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(publicadorEventos).publicar(evento.capture());
        assertThat(evento.getValue()).isInstanceOf(SolicitacaoCanceladaEvent.class);
    }

    @Test
    @DisplayName("falha na transição cancela o comando sem registrar histórico")
    void falhaNaTransicaoNaoRegistraHistorico() {
        when(maquinaEstados.aprovar(solicitacao, gestor, "Aprovado"))
                .thenThrow(new AcessoNegadoException("Somente o aprovador atual pode executar esta ação"));

        AcaoSolicitacao acao = new AprovarSolicitacaoCommand(solicitacao, gestor, "Aprovado",
                maquinaEstados, registradorHistorico, publicadorEventos, RELOGIO);

        assertThatThrownBy(acao::executar).isInstanceOf(AcessoNegadoException.class);
        verifyNoInteractions(registradorHistorico, publicadorEventos);
    }

    @Test
    @DisplayName("invoker executa o comando de forma genérica")
    void invokerExecutaComando() {
        when(maquinaEstados.aprovar(solicitacao, gestor, "Aprovado"))
                .thenReturn(new Transicao(StatusSolicitacao.PENDENTE, StatusSolicitacao.APROVADA, "Solicitação aprovada"));
        AcaoSolicitacao acao = new AprovarSolicitacaoCommand(solicitacao, gestor, "Aprovado",
                maquinaEstados, registradorHistorico, publicadorEventos, RELOGIO);

        ExecutorAcoes executor = new ExecutorAcoes();
        Transicao transicao = executor.executar(acao);

        assertThat(transicao.statusNovo()).isEqualTo(StatusSolicitacao.APROVADA);
        assertThat(acao.solicitacaoId()).isNull();
        assertThat(acao.descricao()).isEqualTo("aprovar");
    }

    /**
     * Utilitário de assertions dos eventos publicados pelos comandos.
     */
    private static final class SolicitationAsserts {

        private SolicitationAsserts() {
        }

        static void check(SolicitacaoAprovadaEvent evento, Solicitacao solicitacao, Usuario aprovador) {
            assertThat(evento.solicitacaoId()).isEqualTo(solicitacao.getId());
            assertThat(evento.solicitanteId()).isEqualTo(solicitacao.getSolicitante().getId());
            assertThat(evento.aprovadorId()).isEqualTo(aprovador.getId());
            assertThat(evento.titulo()).isEqualTo(solicitacao.getTitulo());
            assertThat(evento.ocorridoEm()).isEqualTo(AGORA);
        }
    }
}
