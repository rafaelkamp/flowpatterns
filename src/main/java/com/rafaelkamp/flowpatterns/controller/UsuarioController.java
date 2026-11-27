package com.rafaelkamp.flowpatterns.controller;

import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.common.PageResponse;
import com.rafaelkamp.flowpatterns.dto.usuario.UsuarioRequest;
import com.rafaelkamp.flowpatterns.dto.usuario.UsuarioResponse;
import com.rafaelkamp.flowpatterns.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Gestão dos usuários que participam do fluxo de aprovação")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário criado")
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        return resposta(usuarioService.criar(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um usuário pelo identificador")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return resposta(usuarioService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista usuários com filtros e paginação")
    public PageResponse<UsuarioResponse> listar(
            @Parameter(description = "Filtra por departamento")
            @RequestParam(required = false) String departamento,
            @Parameter(description = "Filtra por papel")
            @RequestParam(required = false) PapelUsuario papel,
            @Parameter(description = "Filtra por situação")
            @RequestParam(required = false) Boolean ativo,
            @Parameter(description = "Busca textual por nome ou e-mail")
            @RequestParam(required = false) String texto,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return PageResponse.from(usuarioService.listar(departamento, papel, ativo, texto, pageable),
                UsuarioController::resposta);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um usuário")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return resposta(usuarioService.atualizar(id, request));
    }

    @PostMapping("/{id}/ativacao")
    @Operation(summary = "Reativa um usuário desativado")
    public UsuarioResponse ativar(@PathVariable Long id) {
        return resposta(usuarioService.ativar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa um usuário (exclusão lógica)")
    public UsuarioResponse desativar(@PathVariable Long id) {
        return resposta(usuarioService.desativar(id));
    }

    private static UsuarioResponse resposta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getDepartamento(), usuario.getPapel(), usuario.isAtivo(),
                usuario.getCriadoEm(), usuario.getAtualizadoEm());
    }
}
