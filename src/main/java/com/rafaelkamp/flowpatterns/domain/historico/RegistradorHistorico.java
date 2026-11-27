package com.rafaelkamp.flowpatterns.domain.historico;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;

/**
 * Port de registro do histórico de auditoria da solicitação.
 */
public interface RegistradorHistorico {

    void registrar(Solicitacao solicitacao, TipoEventoHistorico tipoEvento,
                   StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo,
                   Usuario ator, String detalhe);
}
