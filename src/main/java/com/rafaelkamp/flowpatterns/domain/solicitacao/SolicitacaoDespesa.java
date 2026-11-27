package com.rafaelkamp.flowpatterns.domain.solicitacao;

import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DESPESA")
public class SolicitacaoDespesa extends Solicitacao {

    @Column(precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CategoriaDespesa categoriaDespesa;

    @Column(length = 80)
    private String centroCusto;

    protected SolicitacaoDespesa() {
    }

    private SolicitacaoDespesa(Builder builder) {
        super(builder.titulo, builder.descricao, builder.prioridade, builder.solicitante);
        this.valor = builder.valor;
        this.categoriaDespesa = builder.categoriaDespesa;
        this.centroCusto = builder.centroCusto;
    }

    @Override
    public TipoSolicitacao getTipo() {
        return TipoSolicitacao.DESPESA;
    }

    @Override
    public void validarDadosEspecificos() {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da despesa deve ser maior que zero");
        }
        if (categoriaDespesa == null) {
            throw new RegraNegocioException("A categoria da despesa é obrigatória");
        }
    }

    @Override
    public void atualizarDadosEspecificos(DadosSolicitacao dados) {
        this.valor = dados.valor();
        this.categoriaDespesa = dados.categoriaDespesa();
        this.centroCusto = dados.centroCusto();
    }

    public BigDecimal getValor() {
        return valor;
    }

    public CategoriaDespesa getCategoriaDespesa() {
        return categoriaDespesa;
    }

    public String getCentroCusto() {
        return centroCusto;
    }

    public static class Builder extends SolicitacaoBuilder<SolicitacaoDespesa> {

        private BigDecimal valor;
        private CategoriaDespesa categoriaDespesa;
        private String centroCusto;

        public Builder valor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }

        public Builder categoriaDespesa(CategoriaDespesa categoriaDespesa) {
            this.categoriaDespesa = categoriaDespesa;
            return this;
        }

        public Builder centroCusto(String centroCusto) {
            this.centroCusto = centroCusto;
            return this;
        }

        @Override
        public SolicitacaoDespesa build() {
            return new SolicitacaoDespesa(this);
        }
    }
}
