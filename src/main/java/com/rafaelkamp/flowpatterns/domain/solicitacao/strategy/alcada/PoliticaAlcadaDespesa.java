package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada;

import com.rafaelkamp.flowpatterns.config.AlcadasProperties;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class PoliticaAlcadaDespesa implements PoliticaAlcadaAprovacao {

    private final AlcadasProperties propriedades;

    public PoliticaAlcadaDespesa(AlcadasProperties propriedades) {
        this.propriedades = propriedades;
    }

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.DESPESA;
    }

    @Override
    public NivelAprovacao definirNivel(Solicitacao solicitacao) {
        SolicitacaoDespesa despesa = (SolicitacaoDespesa) solicitacao;
        return despesa.getValor().compareTo(propriedades.despesaLimiteDiretor()) > 0
                ? NivelAprovacao.DIRETOR
                : NivelAprovacao.GESTOR;
    }
}
