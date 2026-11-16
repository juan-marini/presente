package com.github.juanmarini.ms.alunos.dto;

import com.github.juanmarini.ms.alunos.entity.Aluno;
import com.github.juanmarini.ms.alunos.entity.IdiomaFamilia;
import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AlunoResponseDTO {

    private Long id;
    private String matricula;
    private String nome;
    private LocalDate dataNascimento;
    private String turma;
    private String escola;
    private String nomeResponsavel;
    private String telefoneResponsavel;
    private IdiomaFamilia idiomaFamilia;
    private Integer faltasNoBimestre;
    private SituacaoAluno situacao;

    public AlunoResponseDTO(Aluno aluno) {
        id = aluno.getId();
        matricula = aluno.getMatricula();
        nome = aluno.getNome();
        dataNascimento = aluno.getDataNascimento();
        turma = aluno.getTurma();
        escola = aluno.getEscola();
        nomeResponsavel = aluno.getNomeResponsavel();
        telefoneResponsavel = aluno.getTelefoneResponsavel();
        idiomaFamilia = aluno.getIdiomaFamilia();
        faltasNoBimestre = aluno.getFaltasNoBimestre();
        situacao = aluno.getSituacao();
    }
}
