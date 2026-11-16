package com.github.juanmarini.ms.alunos.dto;

import com.github.juanmarini.ms.alunos.entity.IdiomaFamilia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AlunoRequestDTO {

    @NotBlank(message = "A matrícula é obrigatória")
    @Size(min = 5, max = 20, message = "A matrícula deve ter entre 5 e 20 caracteres")
    @Schema(example = "2026EM0099")
    private String matricula;

    @NotBlank(message = "O nome do aluno é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Schema(example = "Lucas Pereira da Silva")
    private String nome;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve estar no passado")
    @Schema(example = "2010-04-15")
    private LocalDate dataNascimento;

    @NotBlank(message = "A turma é obrigatória")
    @Size(min = 2, max = 20, message = "A turma deve ter entre 2 e 20 caracteres")
    @Schema(example = "1A-EM")
    private String turma;

    @NotBlank(message = "A escola é obrigatória")
    @Size(min = 3, max = 120, message = "A escola deve ter entre 3 e 120 caracteres")
    @Schema(example = "E.E. Prof. Maria José")
    private String escola;

    @NotBlank(message = "O nome do responsável é obrigatório")
    @Size(min = 3, max = 100, message = "O nome do responsável deve ter entre 3 e 100 caracteres")
    @Schema(example = "Ana Pereira")
    private String nomeResponsavel;

    @NotBlank(message = "O telefone do responsável é obrigatório")
    @Pattern(regexp = "\\+55\\d{10,11}", message = "O telefone deve estar no formato +55DDDNUMERO")
    @Schema(example = "+5511987654321")
    private String telefoneResponsavel;

    @NotNull(message = "O idioma da família é obrigatório")
    @Schema(example = "PT")
    private IdiomaFamilia idiomaFamilia;

    @NotNull(message = "A quantidade de faltas no bimestre é obrigatória")
    @PositiveOrZero(message = "A quantidade de faltas não pode ser negativa")
    @Schema(example = "3")
    private Integer faltasNoBimestre;
}
