package com.rafaelkamp.flowpatterns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.dto.solicitacao.AcaoLoteResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResponse;
import com.rafaelkamp.flowpatterns.exception.AcessoNegadoException;
import com.rafaelkamp.flowpatterns.exception.GlobalExceptionHandler;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import com.rafaelkamp.flowpatterns.service.SolicitacaoFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Controller - Solicitações")
class SolicitacaoControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SolicitacaoFacade solicitacaoFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SolicitacaoController(solicitacaoFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("cria solicitação e responde 201")
    void criaSolicitacao() throws Exception {
        when(solicitacaoFacade.criar(any())).thenReturn(resposta(StatusSolicitacao.RASCUNHO));

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "tipo", "DESPESA",
                                "titulo", "Material de escritório",
                                "descricao", "Compra de materiais",
                                "prioridade", "MEDIA",
                                "solicitanteId", 1L,
                                "valor", 350.00,
                                "categoriaDespesa", "MATERIAL"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.tipo").value("DESPESA"));
    }

    @Test
    @DisplayName("payload incompleto responde 400")
    void payloadIncompleto() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("tipo", "DESPESA"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.length()").value(4));
    }

    @Test
    @DisplayName("envia solicitação para aprovação")
    void enviaSolicitacao() throws Exception {
        when(solicitacaoFacade.enviar(10L)).thenReturn(resposta(StatusSolicitacao.PENDENTE));

        mockMvc.perform(post("/api/v1/solicitacoes/10/envio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.nivelAprovacaoFinal").value("GESTOR"));
    }

    @Test
    @DisplayName("transição inválida responde 422")
    void transicaoInvalida() throws Exception {
        when(solicitacaoFacade.aprovar(eq(10L), any()))
                .thenThrow(new TransicaoInvalidaException("Rascunhos não podem ser aprovados"));

        mockMvc.perform(post("/api/v1/solicitacoes/10/aprovacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("atorId", 2L, "observacao", "Aprovado"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de negócio não atendida"));
    }

    @Test
    @DisplayName("aprovação por usuário indevido responde 403")
    void aprovacaoIndevida() throws Exception {
        when(solicitacaoFacade.aprovar(eq(10L), any()))
                .thenThrow(new AcessoNegadoException("Somente o aprovador atual pode executar esta ação"));

        mockMvc.perform(post("/api/v1/solicitacoes/10/aprovacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("atorId", 3L))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ação em lote retorna o resumo dos resultados")
    void acaoEmLote() throws Exception {
        AcaoLoteResponse resposta = new AcaoLoteResponse(
                com.rafaelkamp.flowpatterns.domain.solicitacao.TipoAcao.APROVAR, 2, 1, 1,
                List.of(new AcaoLoteResponse.ItemAcao(10L, true, "Solicitação aprovada", "APROVADA"),
                        new AcaoLoteResponse.ItemAcao(11L, false, "Acesso negado", null)));
        when(solicitacaoFacade.executarLote(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/v1/solicitacoes/acoes-lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "acao", "APROVAR",
                                "ids", List.of(10L, 11L),
                                "atorId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.sucesso").value(1))
                .andExpect(jsonPath("$.falha").value(1))
                .andExpect(jsonPath("$.resultados[1].sucesso").value(false));
    }

    @Test
    @DisplayName("busca solicitação por identificador")
    void buscaSolicitacao() throws Exception {
        when(solicitacaoFacade.buscarPorId(10L)).thenReturn(resposta(StatusSolicitacao.PENDENTE));

        mockMvc.perform(get("/api/v1/solicitacoes/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        verify(solicitacaoFacade).buscarPorId(10L);
    }

    private SolicitacaoResponse resposta(StatusSolicitacao status) {
        return new SolicitacaoResponse(10L, TipoSolicitacao.DESPESA, "Material de escritório",
                "Compra de materiais", Prioridade.MEDIA, status, 1L, "Ana Souza", "Financeiro",
                2L, "Gustavo Gestor", NivelAprovacao.GESTOR, NivelAprovacao.GESTOR,
                null, null, null, null, null, null, null, null, null, null,
                Instant.now(), false, Instant.now(), Instant.now(), Instant.now(), null, null);
    }

    private String json(Object payload) throws Exception {
        return objectMapper.writeValueAsString(payload);
    }
}
