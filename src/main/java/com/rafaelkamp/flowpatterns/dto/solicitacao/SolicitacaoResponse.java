package com.rafaelkamp.flowpatterns.dto.solicitacao;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoCompra;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoFerias;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Solicitação retornada pela API")
public record SolicitacaoResponse(
        Long id,
        TipoSolicitacao tipo,
        String titulo,
        String descricao,
        Prioridade prioridade,
        StatusSolicitacao status,
        Long solicitanteId,
        String solicitanteNome,
        String departamentoSolicitante,
        Long aprovadorAtualId,
        String aprovadorAtualNome,
        NivelAprovacao nivelAprovacaoAtual,
        NivelAprovacao nivelAprovacaoFinal,
        BigDecimal valor,
        CategoriaDespesa categoriaDespesa,
        String centroCusto,
        String fornecedor,
        Integer quantidadeItens,
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer diasFerias,
        String sistema,
        NivelAcesso nivelAcesso,
        Instant prazoSla,
        boolean atrasada,
        Instant criadoEm,
        Instant atualizadoEm,
        Instant dataEnvio,
        Instant dataDecisao,
        String observacaoDecisao
) {

    public static SolicitacaoResponse from(Solicitacao solicitacao, Instant agora) {
        SolicitacaoDespesa despesa = solicitacao instanceof SolicitacaoDespesa d ? d : null;
        SolicitacaoCompra compra = solicitacao instanceof SolicitacaoCompra c ? c : null;
        SolicitacaoFerias ferias = solicitacao instanceof SolicitacaoFerias f ? f : null;
        SolicitacaoAcesso acesso = solicitacao instanceof SolicitacaoAcesso a ? a : null;

        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getTipo(),
                solicitacao.getTitulo(),
                solicitacao.getDescricao(),
                solicitacao.getPrioridade(),
                solicitacao.getStatus(),
                solicitacao.getSolicitante().getId(),
                solicitacao.getSolicitante().getNome(),
                solicitacao.getSolicitante().getDepartamento(),
                solicitacao.getAprovadorAtual() == null ? null : solicitacao.getAprovadorAtual().getId(),
                solicitacao.getAprovadorAtual() == null ? null : solicitacao.getAprovadorAtual().getNome(),
                solicitacao.getNivelAprovacaoAtual(),
                solicitacao.getNivelAprovacaoFinal(),
                despesa != null ? despesa.getValor() : (compra != null ? compra.getValor() : null),
                despesa == null ? null : despesa.getCategoriaDespesa(),
                despesa == null ? null : despesa.getCentroCusto(),
                compra == null ? null : compra.getFornecedor(),
                compra == null ? null : compra.getQuantidadeItens(),
                ferias == null ? null : ferias.getDataInicio(),
                ferias == null ? null : ferias.getDataFim(),
                ferias == null ? null : ferias.getDias(),
                acesso == null ? null : acesso.getSistema(),
                acesso == null ? null : acesso.getNivelAcesso(),
                solicitacao.getPrazoSla(),
                solicitacao.estaAtrasada(agora),
                solicitacao.getCriadoEm(),
                solicitacao.getAtualizadoEm(),
                solicitacao.getDataEnvio(),
                solicitacao.getDataDecisao(),
                solicitacao.getObservacaoDecisao()
        );
    }
}
