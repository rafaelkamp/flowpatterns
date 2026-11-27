package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.dto.usuario.UsuarioRequest;
import com.rafaelkamp.flowpatterns.exception.ConflitoException;
import com.rafaelkamp.flowpatterns.exception.RecursoNaoEncontradoException;
import com.rafaelkamp.flowpatterns.exception.RegraNegocioException;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import com.rafaelkamp.flowpatterns.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private static final List<StatusSolicitacao> STATUS_EM_ANDAMENTO = List.of(
            StatusSolicitacao.PENDENTE, StatusSolicitacao.EM_ANALISE);

    private final UsuarioRepository usuarioRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, SolicitacaoRepository solicitacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional
    public Usuario criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflitoException("Já existe um usuário cadastrado com o e-mail " + request.email());
        }
        Usuario usuario = new Usuario(request.nome(), request.email(), request.departamento(), request.papel());
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarAtivo(Long id) {
        Usuario usuario = buscarPorId(id);
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("O usuário " + usuario.getNome() + " está inativo");
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(String departamento, PapelUsuario papel, Boolean ativo, String texto, Pageable pageable) {
        Specification<Usuario> filtro = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (departamento != null && !departamento.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("departamento")), departamento.trim().toLowerCase(Locale.ROOT)));
            }
            if (papel != null) {
                predicates.add(cb.equal(root.get("papel"), papel));
            }
            if (ativo != null) {
                predicates.add(cb.equal(root.get("ativo"), ativo));
            }
            if (texto != null && !texto.isBlank()) {
                String termo = "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nome")), termo),
                        cb.like(cb.lower(root.get("email")), termo)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return usuarioRepository.findAll(filtro, pageable);
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new ConflitoException("Já existe um usuário cadastrado com o e-mail " + request.email());
        }
        usuario.atualizar(request.nome(), request.email(), request.departamento(), request.papel());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario desativar(Long id) {
        Usuario usuario = buscarPorId(id);
        if (solicitacaoRepository.existsByAprovadorAtualIdAndStatusIn(id, STATUS_EM_ANDAMENTO)) {
            throw new ConflitoException(
                    "O usuário possui solicitações pendentes de decisão e não pode ser desativado");
        }
        if (solicitacaoRepository.existsBySolicitanteIdAndStatusIn(id, STATUS_EM_ANDAMENTO)) {
            throw new ConflitoException(
                    "O usuário possui solicitações em andamento e não pode ser desativado");
        }
        usuario.desativar();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.ativar();
        return usuarioRepository.save(usuario);
    }
}
