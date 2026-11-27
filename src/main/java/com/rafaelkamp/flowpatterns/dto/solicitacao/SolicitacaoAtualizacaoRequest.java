package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dados para atualização de uma solicitação em rascunho. " +
        "O tipo e o solicitante não podem ser alterados.")
public record SolicitacaoAtualizacaoRequest(

        @NotBlank
        @Size(max = 140)
        String titulo,

        @NotBlank
        @Size(max = 2000)
        String descricao,

        @NotNull
        Prioridade prioridade,

        @DecimalMin(value = "0.01")
        BigDecimal valor,

        CategoriaDespesa categoriaDespesa,

        @Size(max = 80)
        String centroCusto,

        @Size(max = 120)
        String fornecedor,

        @Min(1)
        Integer quantidadeItens,

        LocalDate dataInicio,

        LocalDate dataFim,

        @Size(max = 100)
        String sistema,

        NivelAcesso nivelAcesso
) {
}
