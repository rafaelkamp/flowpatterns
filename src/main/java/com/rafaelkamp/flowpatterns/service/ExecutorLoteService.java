package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.command.AcaoFactory;
import com.rafaelkamp.flowpatterns.domain.solicitacao.command.AcaoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.command.ExecutorAcoes;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.Transicao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.solicitacao.AcaoLoteRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.AcaoLoteResponse;
import com.rafaelkamp.flowpatterns.exception.RecursoNaoEncontradoException;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Execução de ações em lote. Cada item é processado em uma transação
 * independente, de forma que uma falha não invalida os demais resultados.
 */
@Service
public class ExecutorLoteService {

    private final TransactionTemplate transactionTemplate;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AcaoFactory acaoFactory;
    private final ExecutorAcoes executorAcoes;

    public ExecutorLoteService(PlatformTransactionManager transactionManager,
                               SolicitacaoRepository solicitacaoRepository,
                               UsuarioRepository usuarioRepository,
                               AcaoFactory acaoFactory,
                               ExecutorAcoes executorAcoes) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.acaoFactory = acaoFactory;
        this.executorAcoes = executorAcoes;
    }

    public List<AcaoLoteResponse.ItemAcao> executar(AcaoLoteRequest request) {
        List<AcaoLoteResponse.ItemAcao> resultados = new ArrayList<>();
        for (Long solicitacaoId : request.ids()) {
            try {
                AcaoLoteResponse.ItemAcao item = transactionTemplate.execute(status -> executarItem(solicitacaoId, request));
                resultados.add(item);
            } catch (RuntimeException exception) {
                resultados.add(new AcaoLoteResponse.ItemAcao(solicitacaoId, false, mensagem(exception), null));
            }
        }
        return resultados;
    }

    private AcaoLoteResponse.ItemAcao executarItem(Long solicitacaoId, AcaoLoteRequest request) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", solicitacaoId));
        Usuario ator = usuarioRepository.findById(request.atorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", request.atorId()));
        if (!ator.isAtivo()) {
            throw new RegraNegocioException("O usuário " + ator.getNome() + " está inativo");
        }

        AcaoSolicitacao acao = switch (request.acao()) {
            case APROVAR -> acaoFactory.aprovar(solicitacao, ator, request.observacao());
            case REJEITAR -> acaoFactory.rejeitar(solicitacao, ator, request.observacao());
            case CANCELAR -> acaoFactory.cancelar(solicitacao, ator, request.observacao());
        };

        Transicao transicao = executorAcoes.executar(acao);
        solicitacaoRepository.save(solicitacao);
        return new AcaoLoteResponse.ItemAcao(solicitacaoId, true, transicao.detalhe(),
                transicao.statusNovo().name());
    }

    private String mensagem(RuntimeException exception) {
        String mensagem = exception.getMessage();
        return mensagem == null || mensagem.isBlank() ? exception.getClass().getSimpleName() : mensagem;
    }
}
