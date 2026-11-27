package com.rafaelkamp.flowpatterns.domain.solicitacao;

import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("ACESSO")
public class SolicitacaoAcesso extends Solicitacao {

    @Column(length = 100)
    private String sistema;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NivelAcesso nivelAcesso;

    protected SolicitacaoAcesso() {
    }

    private SolicitacaoAcesso(Builder builder) {
        super(builder.titulo, builder.descricao, builder.prioridade, builder.solicitante);
        this.sistema = builder.sistema;
        this.nivelAcesso = builder.nivelAcesso;
    }

    @Override
    public TipoSolicitacao getTipo() {
        return TipoSolicitacao.ACESSO;
    }

    @Override
    public void validarDadosEspecificos() {
        if (sistema == null || sistema.isBlank()) {
            throw new RegraNegocioException("O sistema de destino do acesso é obrigatório");
        }
        if (nivelAcesso == null) {
            throw new RegraNegocioException("O nível de acesso é obrigatório");
        }
    }

    @Override
    public void atualizarDadosEspecificos(DadosSolicitacao dados) {
        this.sistema = dados.sistema();
        this.nivelAcesso = dados.nivelAcesso();
    }

    public String getSistema() {
        return sistema;
    }

    public NivelAcesso getNivelAcesso() {
        return nivelAcesso;
    }

    public static class Builder extends SolicitacaoBuilder<SolicitacaoAcesso> {

        private String sistema;
        private NivelAcesso nivelAcesso;

        public Builder sistema(String sistema) {
            this.sistema = sistema;
            return this;
        }

        public Builder nivelAcesso(NivelAcesso nivelAcesso) {
            this.nivelAcesso = nivelAcesso;
            return this;
        }

        @Override
        public SolicitacaoAcesso build() {
            return new SolicitacaoAcesso(this);
        }
    }
}
