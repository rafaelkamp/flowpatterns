package com.rafaelkamp.flowpatterns.repository;

import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Usuario> findByDepartamentoAndPapelAndAtivoTrueOrderByIdAsc(String departamento, PapelUsuario papel);

    List<Usuario> findByPapelAndAtivoTrueOrderByIdAsc(PapelUsuario papel);
}
