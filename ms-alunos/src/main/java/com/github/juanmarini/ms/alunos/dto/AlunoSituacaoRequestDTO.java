package com.github.juanmarini.ms.alunos.dto;

import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AlunoSituacaoRequestDTO {

    @NotNull(message = "A situação é obrigatória")
    @Schema(example = "EM_BUSCA_ATIVA")
    private SituacaoAluno situacao;
}
