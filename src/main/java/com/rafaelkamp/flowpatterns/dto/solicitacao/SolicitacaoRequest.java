package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dados para criação de uma solicitação. Os campos específicos " +
        "dependem do tipo: DESPESA (valor, categoriaDespesa, centroCusto), COMPRA (valor, fornecedor, quantidadeItens), " +
        "FERIAS (dataInicio, dataFim) e ACESSO (sistema, nivelAcesso).")
public record SolicitacaoRequest(

        @Schema(description = "Tipo da solicitação", example = "DESPESA")
        @NotNull
        TipoSolicitacao tipo,

        @Schema(description = "Título resumido", example = "Compra de notebooks")
        @NotBlank
        @Size(max = 140)
        String titulo,

        @Schema(description = "Descrição detalhada da solicitação")
        @NotBlank
        @Size(max = 2000)
        String descricao,

        @Schema(description = "Prioridade", example = "ALTA")
        @NotNull
        Prioridade prioridade,

        @Schema(description = "Identificador do solicitante")
        @NotNull
        Long solicitanteId,

        @Schema(description = "Valor financeiro (DESPESA e COMPRA)", example = "1250.00")
        @DecimalMin(value = "0.01")
        BigDecimal valor,

        @Schema(description = "Categoria da despesa (DESPESA)", example = "MATERIAL")
        CategoriaDespesa categoriaDespesa,

        @Schema(description = "Centro de custo (DESPESA)", example = "CC-1020")
        @Size(max = 80)
        String centroCusto,

        @Schema(description = "Fornecedor (COMPRA)", example = "Tech Distribuidora")
        @Size(max = 120)
        String fornecedor,

        @Schema(description = "Quantidade de itens (COMPRA)", example = "3")
        @Min(1)
        Integer quantidadeItens,

        @Schema(description = "Data de início (FERIAS)", example = "2026-12-01")
        LocalDate dataInicio,

        @Schema(description = "Data de fim (FERIAS)", example = "2026-12-15")
        LocalDate dataFim,

        @Schema(description = "Sistema de destino (ACESSO)", example = "ERP")
        @Size(max = 100)
        String sistema,

        @Schema(description = "Nível de acesso (ACESSO)", example = "BASICO")
        NivelAcesso nivelAcesso
) {
}
