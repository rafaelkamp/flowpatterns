package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDadosObrigatorios extends ValidadorEnvio {

    @Override
    protected void validarInterno(ContextoEnvio contexto) {
        Solicitacao solicitacao = contexto.getSolicitacao();
        if (solicitacao.getTitulo() == null || solicitacao.getTitulo().isBlank()) {
            throw new RegraNegocioException("O título da solicitação é obrigatório");
        }
        if (solicitacao.getDescricao() == null || solicitacao.getDescricao().isBlank()) {
            throw new RegraNegocioException("A descrição da solicitação é obrigatória");
        }
        if (solicitacao.getPrioridade() == null) {
            throw new RegraNegocioException("A prioridade da solicitação é obrigatória");
        }
        solicitacao.validarDadosEspecificos();
    }
}
