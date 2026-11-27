package com.rafaelkamp.flowpatterns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResponse;
import com.rafaelkamp.flowpatterns.dto.usuario.UsuarioRequest;
import com.rafaelkamp.flowpatterns.repository.HistoricoSolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.NotificacaoRepository;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.UsuarioRepository;
import com.rafaelkamp.flowpatterns.service.SolicitacaoFacade;
import com.rafaelkamp.flowpatterns.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integração - fluxo completo de solicitações e aprovações")
class FluxoSolicitacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SolicitacaoFacade solicitacaoFacade;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private HistoricoSolicitacaoRepository historicoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    private Usuario solicitante;
    private Usuario gestor;
    private Usuario diretor;

    @BeforeEach
    void setUp() {
        historicoRepository.deleteAll();
        notificacaoRepository.deleteAll();
        solicitacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        solicitante = criarUsuario("Ana Solicitante", "ana@teste.local", "Financeiro", PapelUsuario.SOLICITANTE);
        gestor = criarUsuario("Gustavo Gestor", "gustavo@teste.local", "Financeiro", PapelUsuario.GESTOR);
        diretor = criarUsuario("Helena Diretora", "helena@teste.local", "Diretoria", PapelUsuario.DIRETOR);
    }

    @Test
    @DisplayName("cria solicitação via API e devolve 201 com status RASCUNHO")
    void criaSolicitacaoViaApi() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(despesa("Material de escritório", "350.00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.tipo").value("DESPESA"))
                .andExpect(jsonPath("$.solicitanteNome").value("Ana Solicitante"))
                .andExpect(jsonPath("$.valor").value(350.00))
                .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        assertThat(solicitacaoRepository.findById(Long.valueOf(id))).isPresent();
    }

    @Test
    @DisplayName("envio roteia a solicitação para o gestor do mesmo departamento")
    void envioRoteiaParaGestor() throws Exception {
        Long id = criarDespesa("Material de escritório", "350.00");

        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.nivelAprovacaoFinal").value("GESTOR"))
                .andExpect(jsonPath("$.aprovadorAtualNome").value("Gustavo Gestor"))
                .andExpect(jsonPath("$.prazoSla").isNotEmpty());
    }

    @Test
    @DisplayName("despesa acima do limite percorre duas etapas de aprovação")
    void aprovacaoEmDuasEtapas() throws Exception {
        Long id = criarDespesa("Viagem para conferência", "8000.00");

        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelAprovacaoFinal").value("DIRETOR"))
                .andExpect(jsonPath("$.nivelAprovacaoAtual").value("GESTOR"));

        mockMvc.perform(post("/api/v1/solicitacoes/{id}/aprovacao", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("atorId", gestor.getId(), "observacao", "Aprovado pelo gestor"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.nivelAprovacaoAtual").value("DIRETOR"))
                .andExpect(jsonPath("$.aprovadorAtualNome").value("Helena Diretora"));

        mockMvc.perform(post("/api/v1/solicitacoes/{id}/aprovacao", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("atorId", diretor.getId(), "observacao", "Aprovado pela diretoria"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.dataDecisao").isNotEmpty());

        mockMvc.perform(get("/api/v1/solicitacoes/{id}/historico", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].tipoEvento").value("CRIACAO"))
                .andExpect(jsonPath("$[1].tipoEvento").value("ENVIO"))
                .andExpect(jsonPath("$[2].tipoEvento").value("APROVACAO"))
                .andExpect(jsonPath("$[3].tipoEvento").value("APROVACAO"));
    }

    @Test
    @DisplayName("usuário que não é o aprovador atual recebe 403")
    void aprovacaoIndevidaRetorna403() throws Exception {
        Long id = criarDespesa("Material de escritório", "350.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", id)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/solicitacoes/{id}/aprovacao", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("atorId", diretor.getId(), "observacao", "Tentativa indevida"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));
    }

    @Test
    @DisplayName("envio de solicitação duplicada retorna 409")
    void envioDuplicadoRetorna409() throws Exception {
        Long primeira = criarDespesa("Material de escritório", "350.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", primeira)).andExpect(status().isOk());
        Long segunda = criarDespesa("Material de escritório", "350.00");

        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", segunda))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de estado"));
    }

    @Test
    @DisplayName("dados inválidos do domínio retornam 422")
    void dadosInvalidosRetornam422() throws Exception {
        Map<String, Object> payload = despesa("Sem valor", null);
        payload.remove("valor");

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regra de negócio não atendida"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("valor da despesa")));
    }

    @Test
    @DisplayName("campos obrigatórios ausentes retornam 400 com lista de erros")
    void validacaoDeCamposRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("tipo", "DESPESA", "solicitanteId", solicitante.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.length()").value(3))
                .andExpect(jsonPath("$.erros[0].campo").exists());
    }

    @Test
    @DisplayName("recurso inexistente retorna 404")
    void recursoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/solicitacoes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("listagem aplica filtros e paginação")
    void listagemAplicaFiltros() throws Exception {
        criarDespesa("Material de escritório", "350.00");
        Long compraId = criarCompra("Aquisição de monitores", "15000.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", compraId)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .param("status", "PENDENTE")
                        .param("tipo", "COMPRA")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].tipo").value("COMPRA"));

        mockMvc.perform(get("/api/v1/solicitacoes").param("texto", "Material"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    @DisplayName("aprovador recebe notificação quando a solicitação é enviada")
    void aprovadorRecebeNotificacao() throws Exception {
        Long id = criarDespesa("Material de escritório", "350.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", id)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notificacoes")
                        .param("destinatarioId", String.valueOf(gestor.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("Nova solicitação para aprovação"));

        mockMvc.perform(get("/api/v1/notificacoes/nao-lidas")
                        .param("destinatarioId", String.valueOf(gestor.getId())))
                .andExpect(status().isOk())
                .andExpect(content -> assertThat(content.getResponse().getContentAsString())
                        .isEqualTo("1"));
    }

    @Test
    @DisplayName("ação em lote retorna o resultado individual de cada solicitação")
    void acaoEmLoteRetornaResultadoPorItem() throws Exception {
        Long primeira = criarDespesa("Material A", "100.00");
        Long segunda = criarDespesa("Material B", "200.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", primeira)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", segunda)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/solicitacoes/acoes-lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "acao", "APROVAR",
                                "ids", List.of(primeira, segunda),
                                "atorId", gestor.getId(),
                                "observacao", "Aprovação em lote"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.sucesso").value(2))
                .andExpect(jsonPath("$.falha").value(0))
                .andExpect(jsonPath("$.resultados[0].status").value("APROVADA"));
    }

    @Test
    @DisplayName("ação em lote mantém o resultado dos itens válidos quando um falha")
    void acaoEmLoteIsolaFalhas() throws Exception {
        Long id = criarDespesa("Material A", "100.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", id)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/solicitacoes/acoes-lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "acao", "APROVAR",
                                "ids", List.of(id, 9999L),
                                "atorId", diretor.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(0))
                .andExpect(jsonPath("$.falha").value(2))
                .andExpect(jsonPath("$.resultados[1].sucesso").value(false));
    }

    @Test
    @DisplayName("painel consolida indicadores das solicitações")
    void painelConsolidaIndicadores() throws Exception {
        Long id = criarDespesa("Material de escritório", "350.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", id)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.emAndamento").value(1))
                .andExpect(jsonPath("$.porStatus.PENDENTE").value(1))
                .andExpect(jsonPath("$.porTipo.DESPESA").value(1))
                .andExpect(jsonPath("$.ultimasSolicitacoes.length()").value(1));
    }

    @Test
    @DisplayName("rascunho pode ser excluído e deixa de existir")
    void exclusaoDeRascunho() throws Exception {
        Long id = criarDespesa("Material descartável", "50.00");

        mockMvc.perform(delete("/api/v1/solicitacoes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/solicitacoes/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("solicitação enviada não pode ser excluída")
    void solicitacaoEnviadaNaoPodeSerExcluida() throws Exception {
        Long id = criarDespesa("Material protegido", "50.00");
        mockMvc.perform(post("/api/v1/solicitacoes/{id}/envio", id)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/solicitacoes/{id}", id))
                .andExpect(status().isUnprocessableEntity());
    }

    private Usuario criarUsuario(String nome, String email, String departamento, PapelUsuario papel) {
        return usuarioService.criar(new UsuarioRequest(nome, email, departamento, papel));
    }

    private Long criarDespesa(String titulo, String valor) {
        SolicitacaoResponse solicitacao = solicitacaoFacade.criar(new SolicitacaoRequest(
                TipoSolicitacao.DESPESA, titulo,
                "Descrição da solicitação", Prioridade.MEDIA,
                solicitante.getId(), valor == null ? null : new BigDecimal(valor),
                CategoriaDespesa.MATERIAL,
                "CC-1001", null, null, null, null, null, null));
        return solicitacao.id();
    }

    private Long criarCompra(String titulo, String valor) {
        SolicitacaoResponse solicitacao = solicitacaoFacade.criar(new SolicitacaoRequest(
                TipoSolicitacao.COMPRA, titulo,
                "Descrição da solicitação", Prioridade.ALTA,
                solicitante.getId(), new BigDecimal(valor), null, null, "Fornecedor", 3, null, null, null, null));
        return solicitacao.id();
    }

    private Map<String, Object> despesa(String titulo, String valor) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("tipo", "DESPESA");
        payload.put("titulo", titulo);
        payload.put("descricao", "Descrição da solicitação");
        payload.put("prioridade", "MEDIA");
        payload.put("solicitanteId", solicitante.getId());
        if (valor != null) {
            payload.put("valor", new BigDecimal(valor));
        }
        payload.put("categoriaDespesa", "MATERIAL");
        payload.put("centroCusto", "CC-1001");
        return payload;
    }

    private String json(Object payload) throws Exception {
        return objectMapper.writeValueAsString(payload);
    }
}
