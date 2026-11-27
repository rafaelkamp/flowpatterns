package com.rafaelkamp.flowpatterns.repository;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TiposSolicitacao;
import com.rafaelkamp.flowpatterns.dto.solicitacao.FiltroSolicitacao;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Construção dinâmica dos filtros de consulta de solicitações.
 */
public final class SolicitacaoSpecifications {

    private SolicitacaoSpecifications() {
    }

    public static Specification<Solicitacao> de(FiltroSolicitacao filtro, Instant agora) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.status() != null) {
                predicates.add(cb.equal(root.get("status"), filtro.status()));
            }
            if (filtro.tipo() != null) {
                predicates.add(cb.equal(root.type(), TiposSolicitacao.classeDe(filtro.tipo())));
            }
            if (filtro.prioridade() != null) {
                predicates.add(cb.equal(root.get("prioridade"), filtro.prioridade()));
            }
            if (filtro.solicitanteId() != null) {
                predicates.add(cb.equal(root.get("solicitante").get("id"), filtro.solicitanteId()));
            }
            if (filtro.aprovadorId() != null) {
                predicates.add(cb.equal(root.get("aprovadorAtual").get("id"), filtro.aprovadorId()));
            }
            if (filtro.departamento() != null && !filtro.departamento().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("solicitante").get("departamento")),
                        filtro.departamento().trim().toLowerCase(Locale.ROOT)));
            }
            if (filtro.texto() != null && !filtro.texto().isBlank()) {
                String termo = "%" + filtro.texto().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("titulo")), termo),
                        cb.like(cb.lower(root.get("descricao")), termo)
                ));
            }
            if (Boolean.TRUE.equals(filtro.atrasada())) {
                predicates.add(root.get("status").in(StatusSolicitacao.PENDENTE, StatusSolicitacao.EM_ANALISE));
                predicates.add(cb.lessThan(root.get("prazoSla"), agora));
            }
            if (filtro.criadoDe() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("criadoEm"), filtro.criadoDe()));
            }
            if (filtro.criadoAte() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("criadoEm"), filtro.criadoAte()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
