package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoCriadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.EventosSolicitacao.SolicitacaoEnviadaEvent;
import com.rafaelkamp.flowpatterns.domain.event.PublicadorEventos;
import com.rafaelkamp.flowpatterns.domain.historico.TipoEventoHistorico;
import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ContextoEnvio;
import com.rafaelkamp.flowpatterns.domain.solicitacao.chain.ValidadorEnvio;
import com.rafaelkamp.flowpatterns.domain.solicitacao.command.AcaoFactory;
import com.rafaelkamp.flowpatterns.domain.solicitacao.command.ExecutorAcoes;
import com.rafaelkamp.flowpatterns.domain.solicitacao.factory.SolicitacaoFactoryRegistry;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.DadosEnvioAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.MaquinaEstadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.state.Transicao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.strategy.ServicoPrazoSla;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.common.PageResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.AcaoLoteRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.AcaoLoteResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.CancelamentoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.DecisaoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.FiltroSolicitacao;
import com.rafaelkamp.flowpatterns.dto.solicitacao.HistoricoResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoAtualizacaoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoRequest;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResumoResponse;
import com.rafaelkamp.flowpatterns.exception.RecursoNaoEncontradoException;
import com.rafaelkamp.flowpatterns.exception.TransicaoInvalidaException;
import com.rafaelkamp.flowpatterns.repository.HistoricoSolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.NotificacaoRepository;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * <b>Facade</b> do fluxo de solicitações.
 *
 * <p>Esta classe é a única porta de entrada da camada de aplicação para o
 * ciclo de vida das solicitações. Ela encapsula a coordenação entre a fábrica
 * (Factory Method), a cadeia de validação (Chain of Responsibility), a máquina
 * de estados (State), os comandos (Command) e a publicação de eventos
 * (Observer), expondo uma interface simples para os controllers.</p>
 */
@Service
public class SolicitacaoFacade {

    private final SolicitacaoRepository solicitacaoRepository;
    private final HistoricoSolicitacaoRepository historicoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;
    private final SolicitacaoFactoryRegistry factoryRegistry;
    private final ValidadorEnvio cadeiaValidacaoEnvio;
    private final ServicoPrazoSla servicoPrazoSla;
    private final MaquinaEstadosSolicitacao maquinaEstados;
    private final AcaoFactory acaoFactory;
    private final ExecutorAcoes executorAcoes;
    private final ExecutorLoteService executorLoteService;
    private final PublicadorEventos publicadorEventos;
    private final Clock clock;

    public SolicitacaoFacade(SolicitacaoRepository solicitacaoRepository,
                             HistoricoSolicitacaoRepository historicoRepository,
                             NotificacaoRepository notificacaoRepository,
                             UsuarioService usuarioService,
                             HistoricoService historicoService,
                             SolicitacaoFactoryRegistry factoryRegistry,
                             ValidadorEnvio cadeiaValidacaoEnvio,
                             ServicoPrazoSla servicoPrazoSla,
                             MaquinaEstadosSolicitacao maquinaEstados,
                             AcaoFactory acaoFactory,
                             ExecutorAcoes executorAcoes,
                             ExecutorLoteService executorLoteService,
                             PublicadorEventos publicadorEventos,
                             Clock clock) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioService = usuarioService;
        this.historicoService = historicoService;
        this.factoryRegistry = factoryRegistry;
        this.cadeiaValidacaoEnvio = cadeiaValidacaoEnvio;
        this.servicoPrazoSla = servicoPrazoSla;
        this.maquinaEstados = maquinaEstados;
        this.acaoFactory = acaoFactory;
        this.executorAcoes = executorAcoes;
        this.executorLoteService = executorLoteService;
        this.publicadorEventos = publicadorEventos;
        this.clock = clock;
    }

    @Transactional
    public SolicitacaoResponse criar(SolicitacaoRequest request) {
        Usuario solicitante = usuarioService.buscarAtivo(request.solicitanteId());
        DadosSolicitacao dados = new DadosSolicitacao(request.tipo(), request.titulo(), request.descricao(),
                request.prioridade(), request.valor(), request.categoriaDespesa(), request.centroCusto(),
                request.fornecedor(), request.quantidadeItens(), request.dataInicio(), request.dataFim(),
                request.sistema(), request.nivelAcesso());

        Solicitacao solicitacao = factoryRegistry.criar(dados, solicitante);
        solicitacaoRepository.save(solicitacao);
        historicoService.registrar(solicitacao, TipoEventoHistorico.CRIACAO, null,
                StatusSolicitacao.RASCUNHO, solicitante, "Solicitação criada");
        publicadorEventos.publicar(new SolicitacaoCriadaEvent(solicitacao.getId(), solicitante.getId(),
                solicitacao.getTitulo(), clock.instant()));
        return resposta(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse atualizar(Long id, SolicitacaoAtualizacaoRequest request) {
        Solicitacao solicitacao = buscarEntidade(id);
        maquinaEstados.validarEdicao(solicitacao);

        DadosSolicitacao dados = new DadosSolicitacao(solicitacao.getTipo(), request.titulo(), request.descricao(),
                request.prioridade(), request.valor(), request.categoriaDespesa(), request.centroCusto(),
                request.fornecedor(), request.quantidadeItens(), request.dataInicio(), request.dataFim(),
                request.sistema(), request.nivelAcesso());

        solicitacao.atualizarDadosComuns(dados);
        solicitacao.atualizarDadosEspecificos(dados);
        solicitacao.validarDadosEspecificos();
        solicitacaoRepository.save(solicitacao);
        historicoService.registrar(solicitacao, TipoEventoHistorico.ATUALIZACAO,
                StatusSolicitacao.RASCUNHO, StatusSolicitacao.RASCUNHO, solicitacao.getSolicitante(),
                "Solicitação atualizada");
        return resposta(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse enviar(Long id) {
        Solicitacao solicitacao = buscarEntidade(id);

        ContextoEnvio contexto = new ContextoEnvio(solicitacao);
        cadeiaValidacaoEnvio.validar(contexto);

        Instant prazoSla = servicoPrazoSla.calcular(solicitacao, clock.instant());
        Transicao transicao = maquinaEstados.enviar(solicitacao, new DadosEnvioAprovacao(
                contexto.getNivelFinal(), contexto.getNivelInicial(), contexto.getAprovadorInicial(), prazoSla));

        solicitacaoRepository.save(solicitacao);
        historicoService.registrar(solicitacao, TipoEventoHistorico.ENVIO, transicao.statusAnterior(),
                transicao.statusNovo(), solicitacao.getSolicitante(), transicao.detalhe());
        publicadorEventos.publicar(new SolicitacaoEnviadaEvent(solicitacao.getId(),
                solicitacao.getSolicitante().getId(), contexto.getAprovadorInicial().getId(),
                solicitacao.getTitulo(), contexto.getNivelInicial(), prazoSla, clock.instant()));
        return resposta(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse aprovar(Long id, DecisaoRequest request) {
        Solicitacao solicitacao = buscarEntidade(id);
        Usuario ator = usuarioService.buscarAtivo(request.atorId());
        executorAcoes.executar(acaoFactory.aprovar(solicitacao, ator, request.observacao()));
        solicitacaoRepository.save(solicitacao);
        return resposta(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse rejeitar(Long id, DecisaoRequest request) {
        Solicitacao solicitacao = buscarEntidade(id);
        Usuario ator = usuarioService.buscarAtivo(request.atorId());
        executorAcoes.executar(acaoFactory.rejeitar(solicitacao, ator, request.observacao()));
        solicitacaoRepository.save(solicitacao);
        return resposta(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse cancelar(Long id, CancelamentoRequest request) {
        Solicitacao solicitacao = buscarEntidade(id);
        Usuario ator = usuarioService.buscarAtivo(request.atorId());
        executorAcoes.executar(acaoFactory.cancelar(solicitacao, ator, request.motivo()));
        solicitacaoRepository.save(solicitacao);
        return resposta(solicitacao);
    }

    public AcaoLoteResponse executarLote(AcaoLoteRequest request) {
        List<AcaoLoteResponse.ItemAcao> resultados = executorLoteService.executar(request);
        long sucesso = resultados.stream().filter(AcaoLoteResponse.ItemAcao::sucesso).count();
        return new AcaoLoteResponse(request.acao(), resultados.size(), sucesso,
                resultados.size() - sucesso, resultados);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscarPorId(Long id) {
        return resposta(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<SolicitacaoResumoResponse> listar(FiltroSolicitacao filtro, Pageable pageable) {
        Page<Solicitacao> pagina = solicitacaoRepository.findAll(
                com.rafaelkamp.flowpatterns.repository.SolicitacaoSpecifications.de(filtro, clock.instant()), pageable);
        return PageResponse.from(pagina, solicitacao -> SolicitacaoResumoResponse.from(solicitacao, clock.instant()));
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> historico(Long id) {
        buscarEntidade(id);
        return historicoService.listar(id).stream().map(HistoricoResponse::from).toList();
    }

    @Transactional
    public void excluirRascunho(Long id) {
        Solicitacao solicitacao = buscarEntidade(id);
        if (solicitacao.getStatus() != StatusSolicitacao.RASCUNHO) {
            throw new TransicaoInvalidaException("Somente solicitações em rascunho podem ser excluídas");
        }
        historicoRepository.deleteBySolicitacaoId(id);
        notificacaoRepository.deleteBySolicitacaoId(id);
        solicitacaoRepository.delete(solicitacao);
    }

    private Solicitacao buscarEntidade(Long id) {
        return solicitacaoRepository.buscarComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", id));
    }

    private SolicitacaoResponse resposta(Solicitacao solicitacao) {
        return SolicitacaoResponse.from(solicitacao, clock.instant());
    }
}
