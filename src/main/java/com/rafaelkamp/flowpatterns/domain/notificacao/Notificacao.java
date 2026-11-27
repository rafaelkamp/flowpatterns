package com.rafaelkamp.flowpatterns.domain.notificacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id")
    private Solicitacao solicitacao;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida = false;

    private Instant lidaEm;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    protected Notificacao() {
    }

    public Notificacao(Usuario destinatario, Solicitacao solicitacao, String titulo,
                       String mensagem, Instant criadaEm) {
        this.destinatario = Objects.requireNonNull(destinatario);
        this.solicitacao = solicitacao;
        this.titulo = Objects.requireNonNull(titulo);
        this.mensagem = Objects.requireNonNull(mensagem);
        this.criadaEm = Objects.requireNonNull(criadaEm);
    }

    public void marcarComoLida(Instant agora) {
        if (!lida) {
            this.lida = true;
            this.lidaEm = Objects.requireNonNull(agora);
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean isLida() {
        return lida;
    }

    public Instant getLidaEm() {
        return lidaEm;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
