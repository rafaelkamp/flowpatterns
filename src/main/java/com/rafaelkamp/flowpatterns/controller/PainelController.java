package com.rafaelkamp.flowpatterns.controller;

import com.rafaelkamp.flowpatterns.dto.painel.PainelResponse;
import com.rafaelkamp.flowpatterns.service.PainelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/painel")
@Tag(name = "Painel", description = "Indicadores consolidados do fluxo de solicitações")
public class PainelController {

    private final PainelService painelService;

    public PainelController(PainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping
    @Operation(summary = "Consolida indicadores de solicitações, atrasos e tempo médio de aprovação")
    public PainelResponse painel() {
        return painelService.montar();
    }
}
