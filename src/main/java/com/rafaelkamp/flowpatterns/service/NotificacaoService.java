package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.notificacao.Notificacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.RecursoNaoEncontradoException;
import com.rafaelkamp.flowpatterns.repository.NotificacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final Clock clock;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, Clock clock) {
        this.notificacaoRepository = notificacaoRepository;
        this.clock = clock;
    }

    @Transactional
    public Notificacao criar(Usuario destinatario, Solicitacao solicitacao, String titulo, String mensagem) {
        return notificacaoRepository.save(new Notificacao(destinatario, solicitacao, titulo, mensagem, clock.instant()));
    }

    @Transactional(readOnly = true)
    public Page<Notificacao> listar(Long destinatarioId, Pageable pageable) {
        return notificacaoRepository.findByDestinatarioIdOrderByCriadaEmDesc(destinatarioId, pageable);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long destinatarioId) {
        return notificacaoRepository.countByDestinatarioIdAndLidaFalse(destinatarioId);
    }

    @Transactional
    public Notificacao marcarComoLida(Long notificacaoId, Long destinatarioId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação", notificacaoId));
        if (!Objects.equals(notificacao.getDestinatario().getId(), destinatarioId)) {
            throw new com.rafaelkamp.flowpatterns.exception.AcessoNegadoException(
                    "A notificação pertence a outro usuário");
        }
        notificacao.marcarComoLida(clock.instant());
        return notificacaoRepository.save(notificacao);
    }
}
