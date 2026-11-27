package com.rafaelkamp.flowpatterns.domain.solicitacao.factory;

import com.rafaelkamp.flowpatterns.domain.solicitacao.CategoriaDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.DadosSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Prioridade;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoAcesso;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoCompra;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoDespesa;
import com.rafaelkamp.flowpatterns.domain.solicitacao.SolicitacaoFerias;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Factory Method - criação de solicitações por tipo")
class SolicitacaoFactoryTest {

    private Usuario solicitante;
    private SolicitacaoFactoryRegistry registry;

    @BeforeEach
    void setUp() {
        solicitante = new Usuario("Ana", "ana@teste.local", "Financeiro", PapelUsuario.SOLICITANTE);
        registry = new SolicitacaoFactoryRegistry(List.of(
                new DespesaFactory(), new CompraFactory(), new FeriasFactory(), new AcessoFactory()));
    }

    @Test
    @DisplayName("cria despesa com os dados específicos preenchidos")
    void criaDespesa() {
        DadosSolicitacao dados = new DadosSolicitacao(TipoSolicitacao.DESPESA, "Viagem", "Viagem de trabalho",
                Prioridade.ALTA, new BigDecimal("1250.00"), CategoriaDespesa.VIAGEM, "CC-10",
                null, null, null, null, null, null);

        Solicitacao solicitacao = registry.criar(dados, solicitante);

        assertThat(solicitacao).isInstanceOf(SolicitacaoDespesa.class);
        SolicitacaoDespesa despesa = (SolicitacaoDespesa) solicitacao;
        assertThat(despesa.getTipo()).isEqualTo(TipoSolicitacao.DESPESA);
        assertThat(despesa.getValor()).isEqualByComparingTo("1250.00");
        assertThat(despesa.getCategoriaDespesa()).isEqualTo(CategoriaDespesa.VIAGEM);
        assertThat(despesa.getCentroCusto()).isEqualTo("CC-10");
        assertThat(despesa.getTitulo()).isEqualTo("Viagem");
        assertThat(despesa.getSolicitante()).isSameAs(solicitante);
    }

    @Test
    @DisplayName("cria compra, férias e acesso com seus próprios atributos")
    void criaOutrosTipos() {
        DadosSolicitacao compra = new DadosSolicitacao(TipoSolicitacao.COMPRA, "Notebooks", "Troca de equipamentos",
                Prioridade.MEDIA, new BigDecimal("9000.00"), null, null, "Tech", 5, null, null, null, null);
        DadosSolicitacao ferias = new DadosSolicitacao(TipoSolicitacao.FERIAS, "Férias", "Programação de férias",
                Prioridade.BAIXA, null, null, null, null, null,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15), null, null);
        DadosSolicitacao acesso = new DadosSolicitacao(TipoSolicitacao.ACESSO, "ERP", "Acesso ao ERP",
                Prioridade.URGENTE, null, null, null, null, null, null, null, "ERP", NivelAcesso.ADMINISTRATIVO);

        assertThat(registry.criar(compra, solicitante)).isInstanceOf(SolicitacaoCompra.class);
        assertThat(((SolicitacaoCompra) registry.criar(compra, solicitante)).getQuantidadeItens()).isEqualTo(5);
        assertThat(registry.criar(ferias, solicitante)).isInstanceOf(SolicitacaoFerias.class);
        assertThat(((SolicitacaoFerias) registry.criar(ferias, solicitante)).getDias()).isEqualTo(15);
        assertThat(registry.criar(acesso, solicitante)).isInstanceOf(SolicitacaoAcesso.class);
        assertThat(((SolicitacaoAcesso) registry.criar(acesso, solicitante)).getNivelAcesso())
                .isEqualTo(NivelAcesso.ADMINISTRATIVO);
    }

    @Test
    @DisplayName("rejeita despesa sem valor válido")
    void rejeitaDespesaSemValor() {
        DadosSolicitacao dados = new DadosSolicitacao(TipoSolicitacao.DESPESA, "Sem valor", "Valor ausente",
                Prioridade.MEDIA, BigDecimal.ZERO, CategoriaDespesa.MATERIAL, null,
                null, null, null, null, null, null);

        assertThatThrownBy(() -> registry.criar(dados, solicitante))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("valor da despesa");
    }

    @Test
    @DisplayName("rejeita férias com data final anterior ao início")
    void rejeitaFeriasInvalidas() {
        DadosSolicitacao dados = new DadosSolicitacao(TipoSolicitacao.FERIAS, "Férias", "Datas inválidas",
                Prioridade.MEDIA, null, null, null, null, null,
                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 1), null, null);

        assertThatThrownBy(() -> registry.criar(dados, solicitante))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("data de fim");
    }

    @Test
    @DisplayName("nova solicitação sempre nasce em rascunho")
    void nasceEmRascunho() {
        DadosSolicitacao dados = new DadosSolicitacao(TipoSolicitacao.ACESSO, "Acesso", "Solicitação de acesso",
                Prioridade.MEDIA, null, null, null, null, null, null, null, "ERP", NivelAcesso.BASICO);

        Solicitacao solicitacao = registry.criar(dados, solicitante);

        assertThat(solicitacao.getStatus().name()).isEqualTo("RASCUNHO");
        assertThat(solicitacao.getPrazoSla()).isNull();
        assertThat(solicitacao.getAprovadorAtual()).isNull();
    }
}
