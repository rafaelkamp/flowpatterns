package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoCompra;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoFerias;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.exception.ConflitoException;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ValidadorDuplicidade extends ValidadorEnvio {

    private final SolicitacaoRepository solicitacaoRepository;

    public ValidadorDuplicidade(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Override
    protected void validarInterno(ContextoEnvio contexto) {
        Solicitacao solicitacao = contexto.getSolicitacao();
        Specification<Solicitacao> duplicidade = (root, query, cb) -> cb.and(
                cb.equal(root.type(), classeDoTipo(solicitacao.getTipo())),
                cb.equal(root.get("solicitante").get("id"), solicitacao.getSolicitante().getId()),
                cb.equal(cb.lower(root.get("titulo")), solicitacao.getTitulo().toLowerCase(Locale.ROOT)),
                root.get("status").in(StatusSolicitacao.PENDENTE, StatusSolicitacao.EM_ANALISE),
                cb.notEqual(root.get("id"), solicitacao.getId())
        );

        if (solicitacaoRepository.exists(duplicidade)) {
            throw new ConflitoException(
                    "Já existe uma solicitação em andamento com o mesmo título e tipo para este solicitante");
        }
    }

    private Class<? extends Solicitacao> classeDoTipo(TipoSolicitacao tipo) {
        return switch (tipo) {
            case DESPESA -> SolicitacaoDespesa.class;
            case COMPRA -> SolicitacaoCompra.class;
            case FERIAS -> SolicitacaoFerias.class;
            case ACESSO -> SolicitacaoAcesso.class;
        };
    }
}
