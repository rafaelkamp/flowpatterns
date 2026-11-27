package com.rafaelkamp.flowpatterns.domain.solicitacao.state;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.SeletorAprovador;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.AprovadorIndisponivelException;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;

import java.time.Clock;

/**
 * Estado compartilhado pelas solicitações que aguardam decisão de aprovação.
 */
public abstract class EstadoAguardandoAprovacao extends EstadoSolicitacaoBase {

    protected EstadoAguardandoAprovacao(Clock clock, SeletorAprovador seletorAprovador) {
        super(clock, seletorAprovador);
    }

    @Override
    public Transicao enviar(Solicitacao solicitacao, DadosEnvioAprovacao dados) {
        throw new TransicaoInvalidaException("A solicitação já foi enviada para aprovação");
    }

    @Override
    public Transicao aprovar(Solicitacao solicitacao, Usuario ator, String observacao) {
        validarAprovadorAtual(solicitacao, ator);

        NivelAprovacao nivelAtual = solicitacao.getNivelAprovacaoAtual();
        NivelAprovacao nivelFinal = solicitacao.getNivelAprovacaoFinal();
        if (nivelAtual == NivelAprovacao.GESTOR && nivelFinal == NivelAprovacao.DIRETOR) {
            Usuario diretor = seletorAprovador
                    .selecionar(solicitacao, NivelAprovacao.DIRETOR, solicitacao.getAprovadorAtual().getId())
                    .orElseThrow(() -> new AprovadorIndisponivelException(
                            "Nenhum diretor ou administrador ativo está disponível para a segunda etapa de aprovação"));
            solicitacao.avancarNivel(NivelAprovacao.DIRETOR, diretor);
            return new Transicao(status(), StatusSolicitacao.EM_ANALISE,
                    "Aprovada na etapa do gestor e encaminhada ao diretor");
        }

        solicitacao.aprovar(agora());
        return new Transicao(status(), StatusSolicitacao.APROVADA, "Solicitação aprovada");
    }

    @Override
    public Transicao rejeitar(Solicitacao solicitacao, Usuario ator, String observacao) {
        validarAprovadorAtual(solicitacao, ator);
        solicitacao.rejeitar(observacao, agora());
        return new Transicao(status(), StatusSolicitacao.REJEITADA,
                observacao == null ? "Solicitação rejeitada" : observacao);
    }

    @Override
    public Transicao cancelar(Solicitacao solicitacao, Usuario ator, String motivo) {
        validarSolicitanteOuAdministrador(solicitacao, ator);
        solicitacao.cancelar(motivo, agora());
        return new Transicao(status(), StatusSolicitacao.CANCELADA,
                motivo == null ? "Solicitação cancelada" : motivo);
    }

    @Override
    public void validarEdicao(Solicitacao solicitacao) {
        throw new TransicaoInvalidaException("Somente solicitações em rascunho podem ser editadas");
    }
}
