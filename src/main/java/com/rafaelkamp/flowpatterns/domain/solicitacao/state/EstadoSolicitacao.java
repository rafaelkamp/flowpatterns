package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

/**
 * Estados do padrão <b>State</b>. Cada implementação encapsula as regras
 * permitidas para um status do ciclo de vida da solicitação.
 */
public interface EstadoSolicitacao {

    StatusSolicitacao status();

    Transicao enviar(Solicitacao solicitacao, DadosEnvioAprovacao dados);

    Transicao aprovar(Solicitacao solicitacao, Usuario ator, String observacao);

    Transicao rejeitar(Solicitacao solicitacao, Usuario ator, String observacao);

    Transicao cancelar(Solicitacao solicitacao, Usuario ator, String motivo);

    void validarEdicao(Solicitacao solicitacao);
}
