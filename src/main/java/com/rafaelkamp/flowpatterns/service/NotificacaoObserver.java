package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoAprovadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoCanceladaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoCriadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoEnviadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoRejeitadaEvent;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.UsuarioRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador de eventos de domínio (padrão <b>Observer</b> apoiado pelo Spring).
 * Cada evento publicado pela aplicação é convertido em uma notificação interna
 * para os usuários relacionados, sem acoplar o fluxo de aprovação ao canal de
 * comunicação.
 */
@Component
public class NotificacaoObserver {

    private final NotificacaoService notificacaoService;
    private final UsuarioRepository usuarioRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public NotificacaoObserver(NotificacaoService notificacaoService,
                               UsuarioRepository usuarioRepository,
                               SolicitacaoRepository solicitacaoRepository) {
        this.notificacaoService = notificacaoService;
        this.usuarioRepository = usuarioRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @EventListener
    public void aoCriarSolicitacao(SolicitacaoCriadaEvent evento) {
        notificar(evento.solicitanteId(), evento.solicitacaoId(), "Solicitação criada",
                "A solicitação \"" + evento.titulo() + "\" foi criada em rascunho.");
    }

    @EventListener
    public void aoEnviarSolicitacao(SolicitacaoEnviadaEvent evento) {
        notificar(evento.aprovadorId(), evento.solicitacaoId(), "Nova solicitação para aprovação",
                "A solicitação \"" + evento.titulo() + "\" está aguardando sua aprovação.");
    }

    @EventListener
    public void aoAprovarSolicitacao(SolicitacaoAprovadaEvent evento) {
        if (evento.proximoAprovadorId() != null) {
            notificar(evento.proximoAprovadorId(), evento.solicitacaoId(), "Nova solicitação para aprovação",
                    "A solicitação \"" + evento.titulo() + "\" foi aprovada na etapa anterior e aguarda sua decisão.");
            notificar(evento.solicitanteId(), evento.solicitacaoId(), "Solicitação em análise",
                    "A solicitação \"" + evento.titulo() + "\" foi aprovada na primeira etapa e segue para análise final.");
            return;
        }
        notificar(evento.solicitanteId(), evento.solicitacaoId(), "Solicitação aprovada",
                "A solicitação \"" + evento.titulo() + "\" foi aprovada.");
    }

    @EventListener
    public void aoRejeitarSolicitacao(SolicitacaoRejeitadaEvent evento) {
        notificar(evento.solicitanteId(), evento.solicitacaoId(), "Solicitação rejeitada",
                "A solicitação \"" + evento.titulo() + "\" foi rejeitada. Motivo: "
                        + (evento.motivo() == null ? "não informado" : evento.motivo()));
    }

    @EventListener
    public void aoCancelarSolicitacao(SolicitacaoCanceladaEvent evento) {
        notificar(evento.solicitanteId(), evento.solicitacaoId(), "Solicitação cancelada",
                "A solicitação \"" + evento.titulo() + "\" foi cancelada. Motivo: "
                        + (evento.motivo() == null ? "não informado" : evento.motivo()));
    }

    private void notificar(Long destinatarioId, Long solicitacaoId, String titulo, String mensagem) {
        if (destinatarioId == null) {
            return;
        }
        Usuario destinatario = usuarioRepository.findById(destinatarioId).orElse(null);
        if (destinatario == null) {
            return;
        }
        Solicitacao solicitacao = solicitacaoId == null ? null : solicitacaoRepository.findById(solicitacaoId).orElse(null);
        notificacaoService.criar(destinatario, solicitacao, titulo, mensagem);
    }
}
