package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.usuario.UsuarioRequest;
import com.rafaelkamp.flowpatterns.exception.ConflitoException;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service - regras de usuário")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, solicitacaoRepository);
    }

    @Test
    @DisplayName("cria usuário normalizando o e-mail")
    void criaUsuarioNormalizandoEmail() {
        when(usuarioRepository.existsByEmailIgnoreCase("ANA@EMPRESA.COM")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario usuario = usuarioService.criar(
                new UsuarioRequest("Ana Souza", "ANA@EMPRESA.COM", "Financeiro", PapelUsuario.SOLICITANTE));

        assertThat(usuario.getEmail()).isEqualTo("ana@empresa.com");
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("recusa cadastro com e-mail já existente")
    void recusaEmailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(
                new UsuarioRequest("Ana Souza", "ana@empresa.com", "Financeiro", PapelUsuario.SOLICITANTE)))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("ana@empresa.com");
    }

    @Test
    @DisplayName("impede desativação de usuário com solicitações pendentes como aprovador")
    void impedeDesativacaoComPendencias() {
        Usuario usuario = new Usuario("Ana", "ana@empresa.com", "Financeiro", PapelUsuario.GESTOR);
        usuario.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(solicitacaoRepository.existsByAprovadorAtualIdAndStatusIn(anyLong(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> usuarioService.desativar(1L))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("pendentes");
    }

    @Test
    @DisplayName("desativa usuário sem pendências")
    void desativaUsuario() {
        Usuario usuario = new Usuario("Ana", "ana@empresa.com", "Financeiro", PapelUsuario.SOLICITANTE);
        usuario.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(solicitacaoRepository.existsByAprovadorAtualIdAndStatusIn(anyLong(), any())).thenReturn(false);
        when(solicitacaoRepository.existsBySolicitanteIdAndStatusIn(anyLong(), any())).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario desativado = usuarioService.desativar(1L);

        assertThat(desativado.isAtivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("usuário inativo não pode executar ações")
    void usuarioInativoNaoExecutaAcoes() {
        Usuario usuario = new Usuario("Ana", "ana@empresa.com", "Financeiro", PapelUsuario.SOLICITANTE);
        usuario.setId(1L);
        usuario.desativar();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.buscarAtivo(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    @DisplayName("reativa usuário previamente desativado")
    void reativaUsuario() {
        Usuario usuario = new Usuario("Ana", "ana@empresa.com", "Financeiro", PapelUsuario.SOLICITANTE);
        usuario.setId(1L);
        usuario.desativar();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        assertThat(usuarioService.ativar(1L).isAtivo()).isTrue();
    }

    @Test
    @DisplayName("consulta de pendências considera apenas solicitações em andamento")
    void consultaPendenciasConsideraStatusEmAndamento() {
        List<StatusSolicitacao> esperados = List.of(StatusSolicitacao.PENDENTE, StatusSolicitacao.EM_ANALISE);
        Usuario usuario = new Usuario("Ana", "ana@empresa.com", "Financeiro", PapelUsuario.SOLICITANTE);
        usuario.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(solicitacaoRepository.existsByAprovadorAtualIdAndStatusIn(anyLong(), any())).thenReturn(false);
        when(solicitacaoRepository.existsBySolicitanteIdAndStatusIn(anyLong(), any())).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        usuarioService.desativar(1L);

        verify(solicitacaoRepository).existsBySolicitanteIdAndStatusIn(1L, esperados);
    }
}
