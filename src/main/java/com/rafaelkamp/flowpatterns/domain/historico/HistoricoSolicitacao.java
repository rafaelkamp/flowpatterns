package com.rafaelkamp.flowpatterns.domain.historico;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "historicos_solicitacao")
public class HistoricoSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEventoHistorico tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusSolicitacao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao statusNovo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ator_id")
    private Usuario ator;

    @Column(length = 1000)
    private String detalhe;

    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    protected HistoricoSolicitacao() {
    }

    public HistoricoSolicitacao(Solicitacao solicitacao, TipoEventoHistorico tipoEvento,
                                StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo,
                                Usuario ator, String detalhe, Instant ocorridoEm) {
        this.solicitacao = Objects.requireNonNull(solicitacao);
        this.tipoEvento = Objects.requireNonNull(tipoEvento);
        this.statusAnterior = statusAnterior;
        this.statusNovo = Objects.requireNonNull(statusNovo);
        this.ator = ator;
        this.detalhe = detalhe;
        this.ocorridoEm = Objects.requireNonNull(ocorridoEm);
    }

    public Long getId() {
        return id;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public TipoEventoHistorico getTipoEvento() {
        return tipoEvento;
    }

    public StatusSolicitacao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusSolicitacao getStatusNovo() {
        return statusNovo;
    }

    public Usuario getAtor() {
        return ator;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
