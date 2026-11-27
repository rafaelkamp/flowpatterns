package com.rafaelkamp.flowpatterns.domain.event;

/**
 * Publicador de eventos de domínio (implementado pelo adaptador Spring).
 */
public interface PublicadorEventos {

    void publicar(Object evento);
}
