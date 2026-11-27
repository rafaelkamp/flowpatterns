package com.rafaelkamp.flowpatterns.domain.solicitacao.factory;

import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registro que seleciona o creator concreto de acordo com o tipo solicitado.
 * O Spring injeta todas as implementações de {@link SolicitacaoFactory}.
 */
@Component
public class SolicitacaoFactoryRegistry {

    private final Map<TipoSolicitacao, SolicitacaoFactory> factories;

    public SolicitacaoFactoryRegistry(List<SolicitacaoFactory> factories) {
        this.factories = new EnumMap<>(TipoSolicitacao.class);
        factories.forEach(factory -> this.factories.put(factory.tipo(), factory));
    }

    public Solicitacao criar(DadosSolicitacao dados, Usuario solicitante) {
        SolicitacaoFactory factory = factories.get(dados.tipo());
        if (factory == null) {
            throw new RegraNegocioException("Tipo de solicitação não suportado: " + dados.tipo());
        }
        return factory.criar(dados, solicitante);
    }
}
