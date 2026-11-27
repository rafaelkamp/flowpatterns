package com.rafaelkamp.flowpatterns.domain.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"))
@EntityListeners(AuditingEntityListener.class)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 80)
    private String departamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelUsuario papel;

    @Column(nullable = false)
    private boolean ativo = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant atualizadoEm;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String departamento, PapelUsuario papel) {
        this.nome = Objects.requireNonNull(nome, "nome").trim();
        this.email = normalizarEmail(email);
        this.departamento = Objects.requireNonNull(departamento, "departamento").trim();
        this.papel = Objects.requireNonNull(papel, "papel");
    }

    public void atualizar(String nome, String email, String departamento, PapelUsuario papel) {
        this.nome = Objects.requireNonNull(nome, "nome").trim();
        this.email = normalizarEmail(email);
        this.departamento = Objects.requireNonNull(departamento, "departamento").trim();
        this.papel = Objects.requireNonNull(papel, "papel");
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean podeAprovar() {
        return ativo && (papel == PapelUsuario.GESTOR
                || papel == PapelUsuario.DIRETOR
                || papel == PapelUsuario.ADMINISTRADOR);
    }

    private String normalizarEmail(String email) {
        return Objects.requireNonNull(email, "email").trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Utilizado apenas na reconstituição de objetos em testes e integrações.
     * O identificador é gerado automaticamente pelo banco em produção.
     */
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartamento() {
        return departamento;
    }

    public PapelUsuario getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
