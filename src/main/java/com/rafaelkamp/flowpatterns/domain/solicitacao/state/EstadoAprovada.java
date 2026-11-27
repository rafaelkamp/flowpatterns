package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.SeletorAprovador;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class EstadoAprovada extends EstadoFinal {

    public EstadoAprovada(Clock clock, SeletorAprovador seletorAprovador) {
        super(clock, seletorAprovador);
    }

    @Override
    public StatusSolicitacao status() {
        return StatusSolicitacao.APROVADA;
    }
}
