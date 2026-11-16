package com.github.juanmarini.ms.alunos.tests;

import com.github.juanmarini.ms.alunos.dto.AlunoRequestDTO;
import com.github.juanmarini.ms.alunos.entity.Aluno;
import com.github.juanmarini.ms.alunos.entity.IdiomaFamilia;
import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;

import java.time.LocalDate;

public class Factory {

    public static Aluno createAluno() {
        return new Aluno(
                1L,
                "2026EM0001",
                "Gabriel Souza Lima",
                LocalDate.of(2010, 3, 12),
                "1A-EM",
                "E.E. Prof. Maria José",
                "Cláudia Souza",
                "+5511987650001",
                IdiomaFamilia.PT,
                2,
                SituacaoAluno.REGULAR
        );
    }

    public static AlunoRequestDTO createAlunoRequestDTO() {
        Aluno aluno = createAluno();
        return new AlunoRequestDTO(
                aluno.getMatricula(),
                aluno.getNome(),
                aluno.getDataNascimento(),
                aluno.getTurma(),
                aluno.getEscola(),
                aluno.getNomeResponsavel(),
                aluno.getTelefoneResponsavel(),
                aluno.getIdiomaFamilia(),
                aluno.getFaltasNoBimestre()
        );
    }
}
