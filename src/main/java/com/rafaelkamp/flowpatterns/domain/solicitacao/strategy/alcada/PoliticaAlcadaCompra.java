package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada;

import com.rafaelkamp.flowpatterns.config.AlcadasProperties;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoCompra;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import org.springframework.stereotype.Component;

@Component
public class PoliticaAlcadaCompra implements PoliticaAlcadaAprovacao {

    private final AlcadasProperties propriedades;

    public PoliticaAlcadaCompra(AlcadasProperties propriedades) {
        this.propriedades = propriedades;
    }

    @Override
    public TipoSolicitacao tipo() {
        return TipoSolicitacao.COMPRA;
    }

    @Override
    public NivelAprovacao definirNivel(Solicitacao solicitacao) {
        SolicitacaoCompra compra = (SolicitacaoCompra) solicitacao;
        return compra.getValor().compareTo(propriedades.compraLimiteDiretor()) > 0
                ? NivelAprovacao.DIRETOR
                : NivelAprovacao.GESTOR;
    }
}
