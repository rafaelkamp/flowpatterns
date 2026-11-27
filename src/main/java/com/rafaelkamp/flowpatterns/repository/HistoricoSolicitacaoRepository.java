package com.rafaelkamp.flowpatterns.repository;

import com.rafaelkamp.flowpatterns.domain.historico.HistoricoSolicitacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoSolicitacaoRepository extends JpaRepository<HistoricoSolicitacao, Long> {

    @EntityGraph(attributePaths = "ator")
    List<HistoricoSolicitacao> findBySolicitacaoIdOrderByOcorridoEmAsc(Long solicitacaoId);

    void deleteBySolicitacaoId(Long solicitacaoId);
}
