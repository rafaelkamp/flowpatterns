package com.rafaelkamp.flowpatterns.config;

import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ValidadorAprovador;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ValidadorAlcada;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ValidadorDadosObrigatorios;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ValidadorDuplicidade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ValidadorEnvio;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ValidadorStatusRascunho;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CadeiaValidacaoConfig {

    @Bean
    public ValidadorEnvio cadeiaValidacaoEnvio(ValidadorStatusRascunho statusRascunho,
                                               ValidadorDadosObrigatorios dadosObrigatorios,
                                               ValidadorDuplicidade duplicidade,
                                               ValidadorAlcada alcada,
                                               ValidadorAprovador aprovador) {
        statusRascunho.definirProximo(dadosObrigatorios);
        dadosObrigatorios.definirProximo(duplicidade);
        duplicidade.definirProximo(alcada);
        alcada.definirProximo(aprovador);
        return statusRascunho;
    }
}
