package com.rafaelkamp.flowpatterns.domain.solicitacao.chain;

import com.rafaelkamp.flowpatterns.domain.solicitacao.NivelAprovacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Seleciona o aprovador disponível para o nível exigido.
 *
 * <p>A seleção respeita a seguinte ordem de preferência: mesmo departamento na
 * alçada principal, qualquer departamento na alçada principal, administrador do
 * departamento e, por fim, qualquer administrador. O solicitante e o aprovador
 * atual nunca são escolhidos.</p>
 */
@Component
public class SeletorAprovador {

    private final UsuarioRepository usuarioRepository;

    public SeletorAprovador(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> selecionar(Solicitacao solicitacao, NivelAprovacao nivel) {
        return selecionar(solicitacao, nivel, null);
    }

    public Optional<Usuario> selecionar(Solicitacao solicitacao, NivelAprovacao nivel, Long idExcluido) {
        Long solicitanteId = solicitacao.getSolicitante().getId();
        String departamento = solicitacao.getSolicitante().getDepartamento();

        Map<Long, Usuario> candidatos = new LinkedHashMap<>();
        if (nivel == NivelAprovacao.GESTOR) {
            adicionar(candidatos, usuarioRepository.findByDepartamentoAndPapelAndAtivoTrueOrderByIdAsc(
                    departamento, PapelUsuario.GESTOR));
            adicionar(candidatos, usuarioRepository.findByPapelAndAtivoTrueOrderByIdAsc(PapelUsuario.GESTOR));
        } else {
            adicionar(candidatos, usuarioRepository.findByDepartamentoAndPapelAndAtivoTrueOrderByIdAsc(
                    departamento, PapelUsuario.DIRETOR));
            adicionar(candidatos, usuarioRepository.findByPapelAndAtivoTrueOrderByIdAsc(PapelUsuario.DIRETOR));
        }
        adicionar(candidatos, usuarioRepository.findByDepartamentoAndPapelAndAtivoTrueOrderByIdAsc(
                departamento, PapelUsuario.ADMINISTRADOR));
        adicionar(candidatos, usuarioRepository.findByPapelAndAtivoTrueOrderByIdAsc(PapelUsuario.ADMINISTRADOR));

        return candidatos.values().stream()
                .filter(usuario -> !usuario.getId().equals(solicitanteId))
                .filter(usuario -> idExcluido == null || !usuario.getId().equals(idExcluido))
                .findFirst();
    }

    private void adicionar(Map<Long, Usuario> candidatos, List<Usuario> usuarios) {
        List<Usuario> copia = new ArrayList<>(usuarios);
        copia.forEach(usuario -> candidatos.putIfAbsent(usuario.getId(), usuario));
    }
}
