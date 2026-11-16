package com.github.juanmarini.ms.casos.repository;

import com.github.juanmarini.ms.casos.entity.CasoBuscaAtiva;
import com.github.juanmarini.ms.casos.entity.StatusCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CasoBuscaAtivaRepository extends JpaRepository<CasoBuscaAtiva, Long> {

    List<CasoBuscaAtiva> findByStatus(StatusCaso status);

    List<CasoBuscaAtiva> findByAlunoId(Long alunoId);

    List<CasoBuscaAtiva> findByAlunoIdAndStatus(Long alunoId, StatusCaso status);

    boolean existsByAlunoIdAndStatusIn(Long alunoId, Collection<StatusCaso> status);
}
