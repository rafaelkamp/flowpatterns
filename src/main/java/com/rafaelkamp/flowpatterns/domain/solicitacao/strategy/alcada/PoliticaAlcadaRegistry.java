package com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.alcada;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PoliticaAlcadaRegistry {

    private final Map<TipoSolicitacao, PoliticaAlcadaAprovacao> politicas;

    public PoliticaAlcadaRegistry(List<PoliticaAlcadaAprovacao> politicas) {
        this.politicas = new EnumMap<>(TipoSolicitacao.class);
        politicas.forEach(politica -> this.politicas.put(politica.tipo(), politica));
    }

    public NivelAprovacao definirNivel(Solicitacao solicitacao) {
        PoliticaAlcadaAprovacao politica = politicas.get(solicitacao.getTipo());
        if (politica == null) {
            throw new RegraNegocioException("Não existe política de alçada para o tipo " + solicitacao.getTipo());
        }
        return politica.definirNivel(solicitacao);
    }
}
