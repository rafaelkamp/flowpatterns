package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.SeletorAprovador;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.AcessoNegadoException;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Base común dos estados, concentrate as verificações de autorização.
 */
public abstract class EstadoSolicitacaoBase implements EstadoSolicitacao {

    protected final Clock clock;
    protected final SeletorAprovador seletorAprovador;

    protected EstadoSolicitacaoBase(Clock clock, SeletorAprovador seletorAprovador) {
        this.clock = clock;
        this.seletorAprovador = seletorAprovador;
    }

    protected Instant agora() {
        return clock.instant();
    }

    protected void validarAprovadorAtual(Solicitacao solicitacao, Usuario ator) {
        Usuario aprovadorAtual = solicitacao.getAprovadorAtual();
        if (aprovadorAtual == null || !Objects.equals(aprovadorAtual.getId(), ator.getId())) {
            throw new AcessoNegadoException("Somente o aprovador atual da solicitação pode executar esta ação");
        }
    }

    protected void validarSolicitanteOuAdministrador(Solicitacao solicitacao, Usuario ator) {
        boolean solicitante = Objects.equals(solicitacao.getSolicitante().getId(), ator.getId());
        boolean administrador = ator.getPapel() == PapelUsuario.ADMINISTRADOR;
        if (!solicitante && !administrador) {
            throw new AcessoNegadoException(
                    "Somente o solicitante ou um administrador pode cancelar a solicitação");
        }
    }
}
