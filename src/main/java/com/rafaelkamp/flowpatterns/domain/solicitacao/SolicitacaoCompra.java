package com.rafaelkamp.flowpatterns.domain.solicitacao;

import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("COMPRA")
public class SolicitacaoCompra extends Solicitacao {

    @Column(precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(length = 120)
    private String fornecedor;

    private Integer quantidadeItens;

    protected SolicitacaoCompra() {
    }

    private SolicitacaoCompra(Builder builder) {
        super(builder.titulo, builder.descricao, builder.prioridade, builder.solicitante);
        this.valor = builder.valor;
        this.fornecedor = builder.fornecedor;
        this.quantidadeItens = builder.quantidadeItens;
    }

    @Override
    public TipoSolicitacao getTipo() {
        return TipoSolicitacao.COMPRA;
    }

    @Override
    public void validarDadosEspecificos() {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da compra deve ser maior que zero");
        }
        if (fornecedor == null || fornecedor.isBlank()) {
            throw new RegraNegocioException("O fornecedor da compra é obrigatório");
        }
        if (quantidadeItens == null || quantidadeItens <= 0) {
            throw new RegraNegocioException("A quantidade de itens deve ser maior que zero");
        }
    }

    @Override
    public void atualizarDadosEspecificos(DadosSolicitacao dados) {
        this.valor = dados.valor();
        this.fornecedor = dados.fornecedor();
        this.quantidadeItens = dados.quantidadeItens();
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public Integer getQuantidadeItens() {
        return quantidadeItens;
    }

    public static class Builder extends SolicitacaoBuilder<SolicitacaoCompra> {

        private BigDecimal valor;
        private String fornecedor;
        private Integer quantidadeItens;

        public Builder valor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }

        public Builder fornecedor(String fornecedor) {
            this.fornecedor = fornecedor;
            return this;
        }

        public Builder quantidadeItens(Integer quantidadeItens) {
            this.quantidadeItens = quantidadeItens;
            return this;
        }

        @Override
        public SolicitacaoCompra build() {
            return new SolicitacaoCompra(this);
        }
    }
}
