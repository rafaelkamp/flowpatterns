package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.AprovadorIndisponivelException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ValidadorAprovador extends ValidadorEnvio {

    private final SeletorAprovador seletorAprovador;

    public ValidadorAprovador(SeletorAprovador seletorAprovador) {
        this.seletorAprovador = seletorAprovador;
    }

    @Override
    protected void validarInterno(ContextoEnvio contexto) {
        Solicitacao solicitacao = contexto.getSolicitacao();
        NivelAprovacao nivelFinal = contexto.getNivelFinal();
        if (nivelFinal == null) {
            throw new AprovadorIndisponivelException("Não foi possível determinar o nível de aprovação");
        }

        if (nivelFinal == NivelAprovacao.GESTOR) {
            Usuario gestor = seletorAprovador.selecionar(solicitacao, NivelAprovacao.GESTOR)
                    .orElseThrow(() -> new AprovadorIndisponivelException(
                            "Nenhum gestor ou administrador ativo está disponível para aprovar a solicitação"));
            contexto.setNivelInicial(NivelAprovacao.GESTOR);
            contexto.setAprovadorInicial(gestor);
            return;
        }

        Optional<Usuario> gestor = seletorAprovador.selecionar(solicitacao, NivelAprovacao.GESTOR);
        if (gestor.isPresent()) {
            contexto.setNivelInicial(NivelAprovacao.GESTOR);
            contexto.setAprovadorInicial(gestor.get());
            return;
        }

        Usuario diretor = seletorAprovador.selecionar(solicitacao, NivelAprovacao.DIRETOR)
                .orElseThrow(() -> new AprovadorIndisponivelException(
                        "Nenhum diretor ou administrador ativo está disponível para aprovar a solicitação"));
        contexto.setNivelInicial(NivelAprovacao.DIRETOR);
        contexto.setAprovadorInicial(diretor);
    }
}
