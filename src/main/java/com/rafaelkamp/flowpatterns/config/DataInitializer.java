package com.rafaelkamp.flowpatterns.config;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoRequest;
import com.rafaelkamp.flowpatterns.dto.usuario.UsuarioRequest;
import com.rafaelkamp.flowpatterns.service.SolicitacaoFacade;
import com.rafaelkamp.flowpatterns.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga inicial de demonstração, habilitada pela propriedade
 * {@code flowpatterns.seed.enabled}. Em produção, mantenha-a desabilitada.
 */
@Component
@ConditionalOnProperty(name = "flowpatterns.seed.enabled", havingValue = "true")
public class DataInitializer implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioService usuarioService;
    private final SolicitacaoFacade solicitacaoFacade;

    public DataInitializer(UsuarioService usuarioService, SolicitacaoFacade solicitacaoFacade) {
        this.usuarioService = usuarioService;
        this.solicitacaoFacade = solicitacaoFacade;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarUsuario("Administrador do Sistema", "admin@flowpatterns.local", "TI",
                PapelUsuario.ADMINISTRADOR);
        criarUsuario("Helena Diretora", "diretor@flowpatterns.local", "Diretoria",
                PapelUsuario.DIRETOR);
        criarUsuario("Gustavo Gestor", "gestor.financeiro@flowpatterns.local",
                "Financeiro", PapelUsuario.GESTOR);
        criarUsuario("Gabriela Gestora", "gestor.ti@flowpatterns.local", "TI", PapelUsuario.GESTOR);
        Usuario ana = criarUsuario("Ana Solicitante", "ana@flowpatterns.local", "Financeiro",
                PapelUsuario.SOLICITANTE);
        Usuario bruno = criarUsuario("Bruno Solicitante", "bruno@flowpatterns.local", "Financeiro",
                PapelUsuario.SOLICITANTE);

        criarDespesa(ana, "Material de escritório", "Compra de materiais para o setor financeiro.",
                new BigDecimal("350.00"), CategoriaDespesa.MATERIAL, false);
        criarDespesa(bruno, "Viagem para conferência", "Participação em conferência do setor.",
                new BigDecimal("8000.00"), CategoriaDespesa.VIAGEM, true);
        criarCompra(ana, "Aquisição de monitores", "Reposição de monitores do time financeiro.",
                new BigDecimal("15000.00"), "Tech Distribuidora", 12, true);
        criarAcesso(bruno, "Acesso ao módulo ERP", "Solicitação de acesso ao módulo de compras.",
                "ERP", NivelAcesso.BASICO, true);

        LOG.info("Dados de demonstração criados: 6 usuários e 4 solicitações");
    }

    private Usuario criarUsuario(String nome, String email, String departamento, PapelUsuario papel) {
        return usuarioService.criar(new UsuarioRequest(nome, email, departamento, papel));
    }

    private void criarDespesa(Usuario solicitante, String titulo, String descricao,
                              BigDecimal valor, CategoriaDespesa categoria, boolean enviar) {
        var solicitacao = solicitacaoFacade.criar(new SolicitacaoRequest(
                TipoSolicitacao.DESPESA, titulo, descricao, Prioridade.MEDIA, solicitante.getId(),
                valor, categoria, "CC-1001", null, null, null, null, null, null));
        if (enviar) {
            solicitacaoFacade.enviar(solicitacao.id());
        }
    }

    private void criarCompra(Usuario solicitante, String titulo, String descricao,
                             BigDecimal valor, String fornecedor, int quantidade, boolean enviar) {
        var solicitacao = solicitacaoFacade.criar(new SolicitacaoRequest(
                TipoSolicitacao.COMPRA, titulo, descricao, Prioridade.ALTA, solicitante.getId(),
                valor, null, null, fornecedor, quantidade, null, null, null, null));
        if (enviar) {
            solicitacaoFacade.enviar(solicitacao.id());
        }
    }

    private void criarAcesso(Usuario solicitante, String titulo, String descricao,
                             String sistema, NivelAcesso nivel, boolean enviar) {
        var solicitacao = solicitacaoFacade.criar(new SolicitacaoRequest(
                TipoSolicitacao.ACESSO, titulo, descricao, Prioridade.BAIXA, solicitante.getId(),
                null, null, null, null, null, null, null, sistema, nivel));
        if (enviar) {
            solicitacaoFacade.enviar(solicitacao.id());
        }
    }
}
