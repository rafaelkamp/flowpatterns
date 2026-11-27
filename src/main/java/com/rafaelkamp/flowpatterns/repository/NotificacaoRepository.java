package com.rafaelkamp.flowpatterns.repository;

import com.rafaelkamp.flowpatterns.domain.notificacao.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    @EntityGraph(attributePaths = "solicitacao")
    Page<Notificacao> findByDestinatarioIdOrderByCriadaEmDesc(Long destinatarioId, Pageable pageable);

    long countByDestinatarioIdAndLidaFalse(Long destinatarioId);

    void deleteBySolicitacaoId(Long solicitacaoId);
}
