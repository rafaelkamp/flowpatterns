package com.rafaelkamp.flowpatterns.domain.solicitacao;

import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "solicitacoes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_solicitacao", discriminatorType = DiscriminatorType.STRING, length = 20)
@EntityListeners(AuditingEntityListener.class)
public abstract class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status = StatusSolicitacao.RASCUNHO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovador_atual_id")
    private Usuario aprovadorAtual;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NivelAprovacao nivelAprovacaoAtual;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NivelAprovacao nivelAprovacaoFinal;

    private Instant prazoSla;

    private Instant dataEnvio;

    private Instant dataDecisao;

    @Column(length = 500)
    private String observacaoDecisao;

    @Version
    private Long versao;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant atualizadoEm;

    protected Solicitacao() {
    }

    protected Solicitacao(String titulo, String descricao, Prioridade prioridade, Usuario solicitante) {
        this.titulo = Objects.requireNonNull(titulo, "titulo").trim();
        this.descricao = Objects.requireNonNull(descricao, "descricao").trim();
        this.prioridade = Objects.requireNonNull(prioridade, "prioridade");
        this.solicitante = Objects.requireNonNull(solicitante, "solicitante");
        this.status = StatusSolicitacao.RASCUNHO;
    }

    public abstract TipoSolicitacao getTipo();

    public abstract void validarDadosEspecificos();

    public abstract void atualizarDadosEspecificos(DadosSolicitacao dados);

    public void atualizarDadosComuns(DadosSolicitacao dados) {
        this.titulo = Objects.requireNonNull(dados.titulo(), "titulo").trim();
        this.descricao = Objects.requireNonNull(dados.descricao(), "descricao").trim();
        this.prioridade = Objects.requireNonNull(dados.prioridade(), "prioridade");
    }

    public void enviarParaAprovacao(NivelAprovacao nivelFinal, NivelAprovacao nivelAtual,
                                    Usuario aprovador, Instant prazoSla, Instant agora) {
        if (status != StatusSolicitacao.RASCUNHO) {
            throw new TransicaoInvalidaException("Somente rascunhos podem ser enviados para aprovação");
        }
        this.status = StatusSolicitacao.PENDENTE;
        this.nivelAprovacaoFinal = Objects.requireNonNull(nivelFinal);
        this.nivelAprovacaoAtual = Objects.requireNonNull(nivelAtual);
        this.aprovadorAtual = Objects.requireNonNull(aprovador);
        this.prazoSla = Objects.requireNonNull(prazoSla);
        this.dataEnvio = Objects.requireNonNull(agora);
    }

    public void avancarNivel(NivelAprovacao novoNivel, Usuario novoAprovador) {
        if (!status.emAndamento()) {
            throw new TransicaoInvalidaException("A solicitação não está em andamento");
        }
        this.status = StatusSolicitacao.EM_ANALISE;
        this.nivelAprovacaoAtual = Objects.requireNonNull(novoNivel);
        this.aprovadorAtual = Objects.requireNonNull(novoAprovador);
    }

    public void aprovar(Instant agora) {
        if (!status.emAndamento()) {
            throw new TransicaoInvalidaException("A solicitação não está em andamento");
        }
        this.status = StatusSolicitacao.APROVADA;
        this.dataDecisao = Objects.requireNonNull(agora);
    }

    public void rejeitar(String motivo, Instant agora) {
        if (!status.emAndamento()) {
            throw new TransicaoInvalidaException("A solicitação não está em andamento");
        }
        this.status = StatusSolicitacao.REJEITADA;
        this.dataDecisao = Objects.requireNonNull(agora);
        this.observacaoDecisao = motivo;
    }

    public void cancelar(String motivo, Instant agora) {
        if (status.finalizado()) {
            throw new TransicaoInvalidaException("Solicitações finalizadas não podem ser canceladas");
        }
        this.status = StatusSolicitacao.CANCELADA;
        this.dataDecisao = Objects.requireNonNull(agora);
        this.observacaoDecisao = motivo;
    }

    public boolean estaAtrasada(Instant agora) {
        return status.emAndamento() && prazoSla != null && prazoSla.isBefore(agora);
    }

    /**
     * Utilizado apenas na reconstituição de objetos em testes e integrações.
     */
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Usuario getAprovadorAtual() {
        return aprovadorAtual;
    }

    public NivelAprovacao getNivelAprovacaoAtual() {
        return nivelAprovacaoAtual;
    }

    public NivelAprovacao getNivelAprovacaoFinal() {
        return nivelAprovacaoFinal;
    }

    public Instant getPrazoSla() {
        return prazoSla;
    }

    public Instant getDataEnvio() {
        return dataEnvio;
    }

    public Instant getDataDecisao() {
        return dataDecisao;
    }

    public String getObservacaoDecisao() {
        return observacaoDecisao;
    }

    public Long getVersao() {
        return versao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
