package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoAprovadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoEnviadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoRejeitadaEvent;
import com.rafaelkamp.flowpatterns.domain.notificacao.Notificacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Observer - notificações a partir dos eventos de domínio")
class NotificacaoObserverTest {

    private static final Instant AGORA = Instant.parse("2026-03-10T12:00:00Z");

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    private NotificacaoObserver observer;
    private Usuario solicitante;
    private Usuario aprovador;

    @BeforeEach
    void setUp() {
        observer = new NotificacaoObserver(notificacaoService, usuarioRepository, solicitacaoRepository);
        solicitante = new Usuario("Ana", "ana@teste.local", "Financeiro", PapelUsuario.SOLICITANTE);
        solicitante.setId(1L);
        aprovador = new Usuario("Gustavo", "gustavo@teste.local", "Financeiro", PapelUsuario.GESTOR);
        aprovador.setId(2L);
    }

    @Test
    @DisplayName("envio da solicitação notifica o aprovador")
    void envioNotificaAprovador() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(aprovador));
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.empty());

        observer.aoEnviarSolicitacao(new SolicitacaoEnviadaEvent(10L, 1L, 2L, "Material",
                null, AGORA, AGORA));

        verify(notificacaoService).criar(eq(aprovador), any(), eq("Nova solicitação para aprovação"),
                org.mockito.ArgumentMatchers.contains("Material"));
    }

    @Test
    @DisplayName("aprovação final notifica o solicitante")
    void aprovacaoNotificaSolicitante() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.empty());

        observer.aoAprovarSolicitacao(new SolicitacaoAprovadaEvent(10L, 1L, 2L, "Material",
                "APROVADA", null, AGORA));

        verify(notificacaoService).criar(eq(solicitante), any(), eq("Solicitação aprovada"),
                org.mockito.ArgumentMatchers.contains("Material"));
    }

    @Test
    @DisplayName("aprovação parcial notifica o solicitante e o próximo aprovador")
    void aprovacaoParcialNotificaProximoAprovador() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(aprovador));
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.empty());

        observer.aoAprovarSolicitacao(new SolicitacaoAprovadaEvent(10L, 1L, 2L, "Material",
                "EM_ANALISE", 3L, AGORA));

        verify(notificacaoService).criar(eq(aprovador), any(), eq("Nova solicitação para aprovação"),
                org.mockito.ArgumentMatchers.contains("Material"));
        verify(notificacaoService).criar(eq(solicitante), any(), eq("Solicitação em análise"),
                org.mockito.ArgumentMatchers.contains("Material"));
    }

    @Test
    @DisplayName("rejeição notifica o solicitante com o motivo")
    void rejeicaoNotificaSolicitante() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.empty());

        observer.aoRejeitarSolicitacao(new SolicitacaoRejeitadaEvent(10L, 1L, 2L, "Material",
                "Sem orçamento", AGORA));

        verify(notificacaoService).criar(eq(solicitante), any(), eq("Solicitação rejeitada"),
                org.mockito.ArgumentMatchers.contains("Sem orçamento"));
    }

    @Test
    @DisplayName("evento sem destinatário conhecido não gera notificação")
    void destinatarioInexistenteNaoNotifica() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        observer.aoEnviarSolicitacao(new SolicitacaoEnviadaEvent(10L, 1L, 99L, "Material",
                null, AGORA, AGORA));

        verifyNoInteractions(notificacaoService);
    }
}
