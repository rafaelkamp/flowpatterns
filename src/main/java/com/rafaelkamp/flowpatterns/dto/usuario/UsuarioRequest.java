package com.rafaelkamp.flowpatterns.dto.usuario;

import com.rafaelkamp.flowpatterns.domain.usuario.PapelUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de cadastro e atualização de usuário")
public record UsuarioRequest(

        @Schema(description = "Nome completo", example = "Ana Souza")
        @NotBlank
        @Size(max = 120)
        String nome,

        @Schema(description = "E-mail corporativo", example = "ana.souza@empresa.com")
        @NotBlank
        @Email
        @Size(max = 160)
        String email,

        @Schema(description = "Departamento", example = "Financeiro")
        @NotBlank
        @Size(max = 80)
        String departamento,

        @Schema(description = "Papel do usuário na plataforma")
        @NotNull
        PapelUsuario papel
) {
}
