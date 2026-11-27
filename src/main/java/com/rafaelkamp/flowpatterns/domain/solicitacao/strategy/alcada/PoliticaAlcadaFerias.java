package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada;

import com.rafaelkamp.flowpatterns.config.AlcadasProperties;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoFerias;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class PoliticaAlcadaFerias implements PoliticaAlcadaAprovacao {

    private final AlcadasProperties propriedades;

    public PoliticaAlcadaFerias(AlcadasProperties propriedades) {
        this.propriedades = propriedades;
    }

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.FERIAS;
    }

    @Override
    public NivelAprovacao definirNivel(Solicitacao solicitacao) {
        SolicitacaoFerias ferias = (SolicitacaoFerias) solicitacao;
        return ferias.getDias() > propriedades.feriasDiasLimiteDiretor()
                ? NivelAprovacao.DIRETOR
                : NivelAprovacao.GESTOR;
    }
}
