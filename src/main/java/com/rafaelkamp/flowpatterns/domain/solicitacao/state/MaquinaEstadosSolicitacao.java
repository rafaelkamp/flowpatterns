package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Contexto do padrão <b>State</b>: delega cada transição ao estado atual da
 * solicitação, evitando condicionais espalhadas pelas camadas de serviço.
 */
@Component
public class MaquinaEstadosSolicitacao {

    private final Map<StatusSolicitacao, EstadoSolicitacao> estados = new EnumMap<>(StatusSolicitacao.class);

    public MaquinaEstadosSolicitacao(List<EstadoSolicitacao> estados) {
        estados.forEach(estado -> this.estados.put(estado.status(), estado));
    }

    public Transicao enviar(Solicitacao solicitacao, DadosEnvioAprovacao dados) {
        return estadoAtual(solicitacao).enviar(solicitacao, dados);
    }

    public Transicao aprovar(Solicitacao solicitacao, Usuario ator, String observacao) {
        return estadoAtual(solicitacao).aprovar(solicitacao, ator, observacao);
    }

    public Transicao rejeitar(Solicitacao solicitacao, Usuario ator, String observacao) {
        return estadoAtual(solicitacao).rejeitar(solicitacao, ator, observacao);
    }

    public Transicao cancelar(Solicitacao solicitacao, Usuario ator, String motivo) {
        return estadoAtual(solicitacao).cancelar(solicitacao, ator, motivo);
    }

    public void validarEdicao(Solicitacao solicitacao) {
        estadoAtual(solicitacao).validarEdicao(solicitacao);
    }

    public EstadoSolicitacao estadoAtual(Solicitacao solicitacao) {
        EstadoSolicitacao estado = estados.get(solicitacao.getStatus());
        if (estado == null) {
            throw new TransicaoInvalidaException("Estado desconhecido para a solicitação: " + solicitacao.getStatus());
        }
        return estado;
    }
}
