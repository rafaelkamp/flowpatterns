package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.SeletorAprovador;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.AcessoNegadoException;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("State - ciclo de vida da solicitação")
class MaquinaEstadosSolicitacaoTest {

    private static final Instant AGORA = Instant.parse("2026-03-10T12:00:00Z");
    private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

    @Mock
    private SeletorAprovador seletorAprovador;

    private MaquinaEstadosSolicitacao maquina;
    private Usuario solicitante;
    private Usuario gestor;
    private Usuario diretor;

    @BeforeEach
    void setUp() {
        maquina = new MaquinaEstadosSolicitacao(List.of(
                new EstadoRascunho(RELOGIO, seletorAprovador),
                new EstadoPendente(RELOGIO, seletorAprovador),
                new EstadoEmAnalise(RELOGIO, seletorAprovador),
                new EstadoAprovada(RELOGIO, seletorAprovador),
                new EstadoRejeitada(RELOGIO, seletorAprovador),
                new EstadoCancelada(RELOGIO, seletorAprovador)));

        solicitante = usuario(1L, "Ana", PapelUsuario.SOLICITANTE, "Financeiro");
        gestor = usuario(2L, "Gustavo", PapelUsuario.GESTOR, "Financeiro");
        diretor = usuario(3L, "Helena", PapelUsuario.DIRETOR, "Diretoria");
    }

    @Test
    @DisplayName("rascunho pode ser enviado e passa a pendente")
    void rascunhoEnviadoFicaPendente() {
        Solicitacao solicitacao = rascunho();
        DadosEnvioAprovacao dados = new DadosEnvioAprovacao(NivelAprovacao.GESTOR, NivelAprovacao.GESTOR,
                gestor, AGORA.plusSeconds(86400));

        Transicao transicao = maquina.enviar(solicitacao, dados);

        assertThat(transicao.statusAnterior()).isEqualTo(StatusSolicitacao.RASCUNHO);
        assertThat(transicao.statusNovo()).isEqualTo(StatusSolicitacao.PENDENTE);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
        assertThat(solicitacao.getAprovadorAtual()).isSameAs(gestor);
        assertThat(solicitacao.getDataEnvio()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("aprovação do gestor em alçada de diretor encaminha para o diretor")
    void aprovacaoDoGestorAvancaParaDiretor() {
        Solicitacao solicitacao = pendente(NivelAprovacao.DIRETOR, gestor);
        when(seletorAprovador.selecionar(solicitacao, NivelAprovacao.DIRETOR, gestor.getId()))
                .thenReturn(Optional.of(diretor));

        Transicao transicao = maquina.aprovar(solicitacao, gestor, "Aprovado na primeira etapa");

        assertThat(transicao.statusNovo()).isEqualTo(StatusSolicitacao.EM_ANALISE);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ANALISE);
        assertThat(solicitacao.getNivelAprovacaoAtual()).isEqualTo(NivelAprovacao.DIRETOR);
        assertThat(solicitacao.getAprovadorAtual()).isSameAs(diretor);
    }

    @Test
    @DisplayName("aprovação na última etapa conclui a solicitação")
    void aprovacaoFinalConclui() {
        Solicitacao solicitacao = pendente(NivelAprovacao.GESTOR, gestor);

        Transicao transicao = maquina.aprovar(solicitacao, gestor, "Aprovado");

        assertThat(transicao.statusNovo()).isEqualTo(StatusSolicitacao.APROVADA);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.APROVADA);
        assertThat(solicitacao.getDataDecisao()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("usuário diferente do aprovador atual não pode aprovar")
    void aprovadorIndevidoNaoPodeAprovar() {
        Solicitacao solicitacao = pendente(NivelAprovacao.GESTOR, gestor);

        assertThatThrownBy(() -> maquina.aprovar(solicitacao, diretor, "Aprovado"))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessageContaining("aprovador atual");
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
    }

    @Test
    @DisplayName("rejeição encerra a solicitação em andamento")
    void rejeicaoEncerraSolicitacao() {
        Solicitacao solicitacao = pendente(NivelAprovacao.GESTOR, gestor);

        Transicao transicao = maquina.rejeitar(solicitacao, gestor, "Sem orçamento");

        assertThat(transicao.statusNovo()).isEqualTo(StatusSolicitacao.REJEITADA);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.REJEITADA);
        assertThat(solicitacao.getObservacaoDecisao()).isEqualTo("Sem orçamento");
    }

    @Test
    @DisplayName("terceiro não pode cancelar a solicitação")
    void terceiroNaoPodeCancelar() {
        Solicitacao solicitacao = pendente(NivelAprovacao.GESTOR, gestor);
        Usuario terceiro = usuario(4L, "Carlos", PapelUsuario.SOLICITANTE, "Financeiro");

        assertThatThrownBy(() -> maquina.cancelar(solicitacao, terceiro, "Não quero mais"))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("solicitante cancela a própria solicitação")
    void solicitanteCancela() {
        Solicitacao solicitacao = pendente(NivelAprovacao.GESTOR, gestor);

        Transicao transicao = maquina.cancelar(solicitacao, solicitante, "Pedido equivocado");

        assertThat(transicao.statusNovo()).isEqualTo(StatusSolicitacao.CANCELADA);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.CANCELADA);
    }

    @Test
    @DisplayName("estados finais não permitem novas transições")
    void estadosFinaisNaoPermitemTransicoes() {
        Solicitacao solicitacao = pendente(NivelAprovacao.GESTOR, gestor);
        maquina.aprovar(solicitacao, gestor, "Aprovado");

        assertThatThrownBy(() -> maquina.rejeitar(solicitacao, gestor, "Mudança de ideia"))
                .isInstanceOf(TransicaoInvalidaException.class);
        assertThatThrownBy(() -> maquina.cancelar(solicitacao, solicitante, "Cancelar"))
                .isInstanceOf(TransicaoInvalidaException.class);
        assertThatThrownBy(() -> maquina.validarEdicao(solicitacao))
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    @Test
    @DisplayName("somente rascunhos podem ser editados")
    void somenteRascunhosSaoEditaveis() {
        Solicitacao rascunho = rascunho();
        maquina.validarEdicao(rascunho);

        Solicitacao pendente = pendente(NivelAprovacao.GESTOR, gestor);
        assertThatThrownBy(() -> maquina.validarEdicao(pendente))
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    private Usuario usuario(Long id, String nome, PapelUsuario papel, String departamento) {
        Usuario usuario = new Usuario(nome, nome.toLowerCase() + "@teste.local", departamento, papel);
        usuario.setId(id);
        return usuario;
    }

    private Solicitacao rascunho() {
        return new SolicitacaoDespesa.Builder()
                .valor(new BigDecimal("100.00"))
                .categoriaDespesa(CategoriaDespesa.MATERIAL)
                .dadosComuns("Material", "Descrição", Prioridade.MEDIA, solicitante)
                .build();
    }

    private Solicitacao pendente(NivelAprovacao nivelFinal, Usuario aprovador) {
        Solicitacao solicitacao = rascunho();
        solicitacao.enviarParaAprovacao(nivelFinal, NivelAprovacao.GESTOR, aprovador,
                AGORA.plusSeconds(86400), AGORA);
        return solicitacao;
    }
}
