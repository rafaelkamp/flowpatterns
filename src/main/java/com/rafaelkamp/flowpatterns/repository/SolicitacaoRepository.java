package com.rafaelkamp.flowpatterns.repository;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {

    @EntityGraph(attributePaths = {"solicitante", "aprovadorAtual"})
    @Query("select s from Solicitacao s where s.id = :id")
    Optional<Solicitacao> buscarComRelacionamentos(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"solicitante", "aprovadorAtual"})
    Page<Solicitacao> findAll(Specification<Solicitacao> spec, Pageable pageable);

    boolean existsByAprovadorAtualIdAndStatusIn(Long aprovadorId, Collection<StatusSolicitacao> statuses);

    boolean existsBySolicitanteIdAndStatusIn(Long solicitanteId, Collection<StatusSolicitacao> statuses);

    long countByStatus(StatusSolicitacao status);

    long countByStatusIn(Collection<StatusSolicitacao> statuses);

    long countByStatusInAndPrazoSlaBefore(Collection<StatusSolicitacao> statuses, Instant agora);

    List<Solicitacao> findByStatusAndDataDecisaoAfter(StatusSolicitacao status, Instant after);

    @EntityGraph(attributePaths = {"solicitante", "aprovadorAtual"})
    List<Solicitacao> findTop5ByOrderByCriadoEmDesc();
}
