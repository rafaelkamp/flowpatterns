package com.rafaelkamp.flowpatterns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.usuario.UsuarioRequest;
import com.rafaelkamp.flowpatterns.exception.ConflitoException;
import com.rafaelkamp.flowpatterns.exception.RecursoNaoEncontradoException;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import com.rafaelkamp.flowpatterns.exception.GlobalExceptionHandler;
import com.rafaelkamp.flowpatterns.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Controller - Usuários")
class UsuarioControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("cria usuário e responde 201")
    void criaUsuario() throws Exception {
        when(usuarioService.criar(any(UsuarioRequest.class))).thenReturn(usuario());

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "Ana Souza",
                                "email", "ana.souza@empresa.com",
                                "departamento", "Financeiro",
                                "papel", "SOLICITANTE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.papel").value("SOLICITANTE"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("e-mail inválido responde 400 com detalhes dos erros")
    void emailInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "Ana Souza",
                                "email", "email-invalido",
                                "departamento", "Financeiro",
                                "papel", "SOLICITANTE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.length()").value(1))
                .andExpect(jsonPath("$.erros[0].campo").value("email"));
    }

    @Test
    @DisplayName("e-mail já cadastrado responde 409")
    void emailDuplicado() throws Exception {
        when(usuarioService.criar(any(UsuarioRequest.class)))
                .thenThrow(new ConflitoException("Já existe um usuário cadastrado com o e-mail ana@empresa.com"));

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "Ana Souza",
                                "email", "ana@empresa.com",
                                "departamento", "Financeiro",
                                "papel", "SOLICITANTE"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de estado"));
    }

    @Test
    @DisplayName("usuário inexistente responde 404")
    void usuarioInexistente() throws Exception {
        when(usuarioService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Usuário", 99L));

        mockMvc.perform(get("/api/v1/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("atualiza usuário com sucesso")
    void atualizaUsuario() throws Exception {
        when(usuarioService.atualizar(eq(10L), any(UsuarioRequest.class))).thenReturn(usuario());

        mockMvc.perform(put("/api/v1/usuarios/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nome", "Ana Souza",
                                "email", "ana.souza@empresa.com",
                                "departamento", "Financeiro",
                                "papel", "GESTOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(usuarioService).atualizar(eq(10L), any(UsuarioRequest.class));
    }

    @Test
    @DisplayName("lista usuários paginados")
    void listaUsuarios() throws Exception {
        when(usuarioService.listar(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(usuario())));

        mockMvc.perform(get("/api/v1/usuarios").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Ana Souza"));
    }

    @Test
    @DisplayName("regra de negócio responde 422")
    void regraDeNegocio() throws Exception {
        when(usuarioService.desativar(10L)).thenThrow(new RegraNegocioException("Usuário com solicitações pendentes"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/usuarios/10"))
                .andExpect(status().isUnprocessableEntity());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario("Ana Souza", "ana.souza@empresa.com", "Financeiro", PapelUsuario.SOLICITANTE);
        usuario.setId(10L);
        return usuario;
    }

    private String json(Object payload) throws Exception {
        return objectMapper.writeValueAsString(payload);
    }
}
