package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class PoliticaAlcadaAcesso implements PoliticaAlcadaAprovacao {

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.ACESSO;
    }

    @Override
    public NivelAprovacao definirNivel(Solicitacao solicitacao) {
        SolicitacaoAcesso acesso = (SolicitacaoAcesso) solicitacao;
        return acesso.getNivelAcesso() == NivelAcesso.ADMINISTRATIVO
                ? NivelAprovacao.DIRETOR
                : NivelAprovacao.GESTOR;
    }
}
