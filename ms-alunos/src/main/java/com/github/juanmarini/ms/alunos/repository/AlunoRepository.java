package com.github.juanmarini.ms.alunos.repository;

import com.github.juanmarini.ms.alunos.entity.Aluno;
import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    List<Aluno> findBySituacao(SituacaoAluno situacao);

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);
}
