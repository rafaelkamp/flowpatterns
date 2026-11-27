package com.rafaelkamp.flowpatterns.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super("%s não encontrado(a) para o identificador %s".formatted(recurso, id));
    }

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
