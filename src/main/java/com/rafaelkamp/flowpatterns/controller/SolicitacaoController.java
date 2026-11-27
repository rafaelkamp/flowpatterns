package com.rafaelkamp.flowpatterns.controller;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.dto.common.PageResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.AcaoLoteRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.AcaoLoteResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.CancelamentoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.DecisaoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.FiltroSolicitacao;
import com.rafaelkamp.flowpatterns.dto.solicitacao.HistoricoResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoAtualizacaoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResumoResponse;
import com.rafaelkamp.flowpatterns.service.SolicitacaoFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@Tag(name = "Solicitações", description = "Fluxo completo de solicitação, aprovação e cancelamento")
public class SolicitacaoController {

    private final SolicitacaoFacade solicitacaoFacade;

    public SolicitacaoController(SolicitacaoFacade solicitacaoFacade) {
        this.solicitacaoFacade = solicitacaoFacade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma solicitação em rascunho")
    @ApiResponse(responseCode = "201", description = "Solicitação criada")
    @ApiResponse(responseCode = "422", description = "Dados inválidos para o tipo informado")
    public SolicitacaoResponse criar(@Valid @RequestBody SolicitacaoRequest request) {
        return solicitacaoFacade.criar(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma solicitação pelo identificador")
    public SolicitacaoResponse buscar(@PathVariable Long id) {
        return solicitacaoFacade.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Lista solicitações com filtros, ordenação e paginação")
    public PageResponse<SolicitacaoResumoResponse> listar(
            @Parameter(description = "Filtra por status")
            @RequestParam(required = false) StatusSolicitacao status,
            @Parameter(description = "Filtra por tipo")
            @RequestParam(required = false) TipoSolicitacao tipo,
            @Parameter(description = "Filtra por prioridade")
            @RequestParam(required = false) Prioridade prioridade,
            @Parameter(description = "Filtra pelo solicitante")
            @RequestParam(required = false) Long solicitanteId,
            @Parameter(description = "Filtra pelo aprovador atual")
            @RequestParam(required = false) Long aprovadorId,
            @Parameter(description = "Filtra pelo departamento do solicitante")
            @RequestParam(required = false) String departamento,
            @Parameter(description = "Busca textual em título e descrição")
            @RequestParam(required = false) String texto,
            @Parameter(description = "Lista apenas solicitações atrasadas")
            @RequestParam(required = false) Boolean atrasada,
            @Parameter(description = "Data inicial de criação (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant criadoDe,
            @Parameter(description = "Data final de criação (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant criadoAte,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable) {
        FiltroSolicitacao filtro = new FiltroSolicitacao(status, tipo, prioridade, solicitanteId, aprovadorId,
                departamento, texto, atrasada, criadoDe, criadoAte);
        return solicitacaoFacade.listar(filtro, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma solicitação em rascunho")
    public SolicitacaoResponse atualizar(@PathVariable Long id,
                                         @Valid @RequestBody SolicitacaoAtualizacaoRequest request) {
        return solicitacaoFacade.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui uma solicitação que ainda está em rascunho")
    public void excluir(@PathVariable Long id) {
        solicitacaoFacade.excluirRascunho(id);
    }

    @PostMapping("/{id}/envio")
    @Operation(summary = "Envia a solicitação para aprovação")
    @ApiResponse(responseCode = "200", description = "Solicitação enviada")
    @ApiResponse(responseCode = "409", description = "Solicitação duplicada em andamento")
    @ApiResponse(responseCode = "422", description = "Solicitação não elegível para envio")
    public SolicitacaoResponse enviar(@PathVariable Long id) {
        return solicitacaoFacade.enviar(id);
    }

    @PostMapping("/{id}/aprovacao")
    @Operation(summary = "Aprova a solicitação (ou avança a etapa de alçada)")
    public SolicitacaoResponse aprovar(@PathVariable Long id, @Valid @RequestBody DecisaoRequest request) {
        return solicitacaoFacade.aprovar(id, request);
    }

    @PostMapping("/{id}/rejeicao")
    @Operation(summary = "Rejeita a solicitação")
    public SolicitacaoResponse rejeitar(@PathVariable Long id, @Valid @RequestBody DecisaoRequest request) {
        return solicitacaoFacade.rejeitar(id, request);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela a solicitação")
    public SolicitacaoResponse cancelar(@PathVariable Long id, @Valid @RequestBody CancelamentoRequest request) {
        return solicitacaoFacade.cancelar(id, request);
    }

    @PostMapping("/acoes-lote")
    @Operation(summary = "Executa uma ação (aprovar, rejeitar ou cancelar) em lote")
    @ApiResponse(responseCode = "200", description = "Resultado individual de cada solicitação")
    public AcaoLoteResponse executarLote(@Valid @RequestBody AcaoLoteRequest request) {
        return solicitacaoFacade.executarLote(request);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Lista o histórico de auditoria da solicitação")
    public List<HistoricoResponse> historico(@PathVariable Long id) {
        return solicitacaoFacade.historico(id);
    }
}
