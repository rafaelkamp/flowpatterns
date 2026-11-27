package com.rafaelkamp.flowpatterns.domain.solicitacao;

import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@DiscriminatorValue("FERIAS")
public class SolicitacaoFerias extends Solicitacao {

    @Column(name = "ferias_data_inicio")
    private LocalDate dataInicio;

    @Column(name = "ferias_data_fim")
    private LocalDate dataFim;

    protected SolicitacaoFerias() {
    }

    private SolicitacaoFerias(Builder builder) {
        super(builder.titulo, builder.descricao, builder.prioridade, builder.solicitante);
        this.dataInicio = builder.dataInicio;
        this.dataFim = builder.dataFim;
    }

    @Override
    public TipoSolicitacao getTipo() {
        return TipoSolicitacao.FERIAS;
    }

    @Override
    public void validarDadosEspecificos() {
        if (dataInicio == null || dataFim == null) {
            throw new RegraNegocioException("As datas de início e fim das férias são obrigatórias");
        }
        if (!dataFim.isAfter(dataInicio)) {
            throw new RegraNegocioException("A data de fim das férias deve ser posterior à data de início");
        }
    }

    @Override
    public void atualizarDadosEspecificos(DadosSolicitacao dados) {
        this.dataInicio = dados.dataInicio();
        this.dataFim = dados.dataFim();
    }

    public int getDias() {
        if (dataInicio == null || dataFim == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public static class Builder extends SolicitacaoBuilder<SolicitacaoFerias> {

        private LocalDate dataInicio;
        private LocalDate dataFim;

        public Builder dataInicio(LocalDate dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public Builder dataFim(LocalDate dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        @Override
        public SolicitacaoFerias build() {
            return new SolicitacaoFerias(this);
        }
    }
}
