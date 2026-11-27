package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada.PoliticaAlcadaRegistry;
import org.springframework.stereotype.Component;

@Component
public class ValidadorAlcada extends ValidadorEnvio {

    private final PoliticaAlcadaRegistry politicaAlcadaRegistry;

    public ValidadorAlcada(PoliticaAlcadaRegistry politicaAlcadaRegistry) {
        this.politicaAlcadaRegistry = politicaAlcadaRegistry;
    }

    @Override
    protected void validarInterno(ContextoEnvio contexto) {
        contexto.setNivelFinal(politicaAlcadaRegistry.definirNivel(contexto.getSolicitacao()));
    }
}
