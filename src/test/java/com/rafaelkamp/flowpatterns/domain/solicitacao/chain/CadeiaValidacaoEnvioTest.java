package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada.PoliticaAlcadaRegistry;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.ConflitoException;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Chain of Responsibility - validação e roteamento do envio")
class CadeiaValidacaoEnvioTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @Mock
    private PoliticaAlcadaRegistry politicaAlcadaRegistry;

    @Mock
    private SeletorAprovador seletorAprovador;

    private Usuario solicitante;
    private Usuario gestor;
    private ValidadorEnvio cadeia;

    @BeforeEach
    void setUp() {
        solicitante = new Usuario("Ana", "ana@teste.local", "Financeiro", PapelUsuario.SOLICITANTE);
        solicitante.setId(1L);
        gestor = new Usuario("Gustavo", "gustavo@teste.local", "Financeiro", PapelUsuario.GESTOR);
        gestor.setId(2L);

        ValidadorStatusRascunho statusRascunho = new ValidadorStatusRascunho();
        ValidadorDadosObrigatorios dadosObrigatorios = new ValidadorDadosObrigatorios();
        ValidadorDuplicidade duplicidade = new ValidadorDuplicidade(solicitacaoRepository);
        ValidadorAlcada alcada = new ValidadorAlcada(politicaAlcadaRegistry);
        ValidadorAprovador aprovador = new ValidadorAprovador(seletorAprovador);

        statusRascunho.definirProximo(dadosObrigatorios);
        dadosObrigatorios.definirProximo(duplicidade);
        duplicidade.definirProximo(alcada);
        alcada.definirProximo(aprovador);

        cadeia = statusRascunho;
    }

    @Test
    @DisplayName("cadeia completa define alçada, nível inicial e aprovador")
    void cadeiaCompletaRoteiaSolicitacao() {
        Solicitacao solicitacao = despesa();
        when(solicitacaoRepository.exists(any(Specification.class))).thenReturn(false);
        when(politicaAlcadaRegistry.definirNivel(solicitacao)).thenReturn(NivelAprovacao.GESTOR);
        when(seletorAprovador.selecionar(solicitacao, NivelAprovacao.GESTOR)).thenReturn(Optional.of(gestor));

        ContextoEnvio contexto = new ContextoEnvio(solicitacao);
        cadeia.validar(contexto);

        assertThat(contexto.getNivelFinal()).isEqualTo(NivelAprovacao.GESTOR);
        assertThat(contexto.getNivelInicial()).isEqualTo(NivelAprovacao.GESTOR);
        assertThat(contexto.getAprovadorInicial()).isSameAs(gestor);
    }

    @Test
    @DisplayName("solicitação fora de rascunho interrompe a cadeia antes das demais validações")
    void interrompeCadeiaParaStatusInvalido() {
        Solicitacao solicitacao = despesa();
        solicitacao.enviarParaAprovacao(NivelAprovacao.GESTOR, NivelAprovacao.GESTOR, gestor,
                java.time.Instant.now(), java.time.Instant.now());

        ContextoEnvio contexto = new ContextoEnvio(solicitacao);

        assertThatThrownBy(() -> cadeia.validar(contexto))
                .isInstanceOf(TransicaoInvalidaException.class);
        verify(solicitacaoRepository, never()).exists(any(Specification.class));
    }

    @Test
    @DisplayName("duplicidade bloqueia o envio")
    void bloqueiaDuplicidade() {
        Solicitacao solicitacao = despesa();
        when(solicitacaoRepository.exists(any(Specification.class))).thenReturn(true);

        ContextoEnvio contexto = new ContextoEnvio(solicitacao);

        assertThatThrownBy(() -> cadeia.validar(contexto))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("mesmo título");
        verify(politicaAlcadaRegistry, never()).definirNivel(any());
    }

    @Test
    @DisplayName("alçada de diretor inicia o fluxo no gestor quando existir")
    void alçadaDeDiretorIniciaNoGestor() {
        Solicitacao solicitacao = despesa();
        when(solicitacaoRepository.exists(any(Specification.class))).thenReturn(false);
        when(politicaAlcadaRegistry.definirNivel(solicitacao)).thenReturn(NivelAprovacao.DIRETOR);
        when(seletorAprovador.selecionar(solicitacao, NivelAprovacao.GESTOR)).thenReturn(Optional.of(gestor));

        ContextoEnvio contexto = new ContextoEnvio(solicitacao);
        cadeia.validar(contexto);

        assertThat(contexto.getNivelFinal()).isEqualTo(NivelAprovacao.DIRETOR);
        assertThat(contexto.getNivelInicial()).isEqualTo(NivelAprovacao.GESTOR);
    }

    @Test
    @DisplayName("sem gestor disponível, a alçada de diretor começa diretamente no diretor")
    void alçadaDeDiretorSemGestorComeçaNoDiretor() {
        Solicitacao solicitacao = despesa();
        Usuario diretor = new Usuario("Helena", "helena@teste.local", "Diretoria", PapelUsuario.DIRETOR);
        diretor.setId(3L);
        when(solicitacaoRepository.exists(any(Specification.class))).thenReturn(false);
        when(politicaAlcadaRegistry.definirNivel(solicitacao)).thenReturn(NivelAprovacao.DIRETOR);
        when(seletorAprovador.selecionar(solicitacao, NivelAprovacao.GESTOR)).thenReturn(Optional.empty());
        when(seletorAprovador.selecionar(solicitacao, NivelAprovacao.DIRETOR)).thenReturn(Optional.of(diretor));

        ContextoEnvio contexto = new ContextoEnvio(solicitacao);
        cadeia.validar(contexto);

        assertThat(contexto.getNivelInicial()).isEqualTo(NivelAprovacao.DIRETOR);
        assertThat(contexto.getAprovadorInicial()).isSameAs(diretor);
    }

    private Solicitacao despesa() {
        Solicitacao solicitacao = new SolicitacaoDespesa.Builder()
                .valor(new BigDecimal("100.00"))
                .categoriaDespesa(CategoriaDespesa.MATERIAL)
                .dadosComuns("Material", "Descrição", Prioridade.MEDIA, solicitante)
                .build();
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.RASCUNHO);
        return solicitacao;
    }
}
