package com.rafaelkamp.flowpatterns.domain.solicitacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados de entrada do domínio para criação e atualização de solicitações.
 * Campos não aplicáveis ao tipo escolhido são ignorados pelas fábricas.
 */
public record DadosSolicitacao(
        TipoSolicitacao tipo,
        String titulo,
        String descricao,
        Prioridade prioridade,
        BigDecimal valor,
        CategoriaDespesa categoriaDespesa,
        String centroCusto,
        String fornecedor,
        Integer quantidadeItens,
        LocalDate dataInicio,
        LocalDate dataFim,
        String sistema,
        NivelAcesso nivelAcesso
) {
}
