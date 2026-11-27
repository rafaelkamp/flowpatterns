package com.rafaelkamp.flowpatterns.controller;

import com.rafaelkamp.flowpatterns.dto.common.PageResponse;
import com.rafaelkamp.flowpatterns.dto.notificacao.NotificacaoResponse;
import com.rafaelkamp.flowpatterns.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificacoes")
@Tag(name = "Notificações", description = "Caixa de entrada interna dos usuários")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    @Operation(summary = "Lista as notificações de um usuário")
    public PageResponse<NotificacaoResponse> listar(
            @Parameter(description = "Usuário destinatário", required = true)
            @RequestParam Long destinatarioId,
            @PageableDefault(size = 20, sort = "criadaEm") Pageable pageable) {
        return PageResponse.from(notificacaoService.listar(destinatarioId, pageable), NotificacaoResponse::from);
    }

    @GetMapping("/nao-lidas")
    @Operation(summary = "Conta as notificações não lidas de um usuário")
    public long contarNaoLidas(@RequestParam Long destinatarioId) {
        return notificacaoService.contarNaoLidas(destinatarioId);
    }

    @PostMapping("/{id}/leitura")
    @Operation(summary = "Marca uma notificação como lida")
    public NotificacaoResponse marcarComoLida(
            @PathVariable Long id,
            @Parameter(description = "Usuário destinatário", required = true)
            @RequestParam Long destinatarioId) {
        return NotificacaoResponse.from(notificacaoService.marcarComoLida(id, destinatarioId));
    }
}
