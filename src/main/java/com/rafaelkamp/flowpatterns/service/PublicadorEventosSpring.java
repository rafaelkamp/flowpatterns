package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.event.PublicadorEventos;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador que conecta a porta de publicação de eventos ao suporte de eventos
 * do Spring Framework.
 */
@Component
public class PublicadorEventosSpring implements PublicadorEventos {

    private final ApplicationEventPublisher publisher;

    public PublicadorEventosSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(Object evento) {
        publisher.publishEvent(evento);
    }
}
