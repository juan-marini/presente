package com.github.juanmarini.ms.alunos.service;

import com.github.juanmarini.ms.alunos.dto.AlunoRequestDTO;
import com.github.juanmarini.ms.alunos.dto.AlunoResponseDTO;
import com.github.juanmarini.ms.alunos.entity.Aluno;
import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;
import com.github.juanmarini.ms.alunos.exceptions.AlunoEmBuscaAtivaException;
import com.github.juanmarini.ms.alunos.exceptions.DatabaseException;
import com.github.juanmarini.ms.alunos.exceptions.ResourceNotFoundException;
import com.github.juanmarini.ms.alunos.exceptions.SituacaoInvalidaException;
import com.github.juanmarini.ms.alunos.repository.AlunoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlunoService {

    @Autowired
    private AlunoRepository alunoRepository;

    @Transactional(readOnly = true)
    public List<AlunoResponseDTO> findAllAlunos(SituacaoAluno situacao) {
        List<Aluno> alunos = (situacao == null)
                ? alunoRepository.findAll()
                : alunoRepository.findBySituacao(situacao);
        return alunos.stream().map(AlunoResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public AlunoResponseDTO findAlunoById(Long id) {
        Aluno aluno = alunoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Aluno não encontrado. ID: " + id)
        );
        return new AlunoResponseDTO(aluno);
    }

    @Transactional
    public AlunoResponseDTO saveAluno(AlunoRequestDTO requestDTO) {
        if (alunoRepository.existsByMatricula(requestDTO.getMatricula())) {
            throw new DatabaseException("Já existe um aluno com a matrícula " + requestDTO.getMatricula());
        }
        Aluno aluno = new Aluno();
        copyDtoToAluno(requestDTO, aluno);
        aluno.setSituacao(SituacaoAluno.REGULAR);
        aluno = alunoRepository.save(aluno);
        return new AlunoResponseDTO(aluno);
    }

    @Transactional
    public AlunoResponseDTO updateAluno(Long id, AlunoRequestDTO requestDTO) {
        if (!alunoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aluno não encontrado. ID: " + id);
        }
        if (alunoRepository.existsByMatriculaAndIdNot(requestDTO.getMatricula(), id)) {
            throw new DatabaseException("Já existe um aluno com a matrícula " + requestDTO.getMatricula());
        }
        try {
            Aluno aluno = alunoRepository.getReferenceById(id);
            copyDtoToAluno(requestDTO, aluno);
            aluno = alunoRepository.save(aluno);
            return new AlunoResponseDTO(aluno);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Aluno não encontrado. ID: " + id);
        }
    }

    @Transactional
    public void deleteAluno(Long id) {
        Aluno aluno = alunoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Aluno não encontrado. ID: " + id)
        );
        if (aluno.getSituacao() == SituacaoAluno.EM_BUSCA_ATIVA) {
            throw new AlunoEmBuscaAtivaException(
                    String.format("Aluno id %d está em busca ativa e não pode ser excluído", id));
        }
        alunoRepository.deleteById(id);
    }

    @Transactional
    public AlunoResponseDTO alterarSituacao(Long id, SituacaoAluno novaSituacao) {
        Aluno aluno = alunoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Aluno não encontrado. ID: " + id)
        );
        if (!aluno.getSituacao().podeMudarPara(novaSituacao)) {
            throw new SituacaoInvalidaException(
                    String.format("Situação do aluno id %d não pode mudar de %s para %s",
                            id, aluno.getSituacao(), novaSituacao));
        }
        aluno.setSituacao(novaSituacao);
        aluno = alunoRepository.save(aluno);
        return new AlunoResponseDTO(aluno);
    }

    private void copyDtoToAluno(AlunoRequestDTO requestDTO, Aluno aluno) {
        aluno.setMatricula(requestDTO.getMatricula());
        aluno.setNome(requestDTO.getNome());
        aluno.setDataNascimento(requestDTO.getDataNascimento());
        aluno.setTurma(requestDTO.getTurma());
        aluno.setEscola(requestDTO.getEscola());
        aluno.setNomeResponsavel(requestDTO.getNomeResponsavel());
        aluno.setTelefoneResponsavel(requestDTO.getTelefoneResponsavel());
        aluno.setIdiomaFamilia(requestDTO.getIdiomaFamilia());
        aluno.setFaltasNoBimestre(requestDTO.getFaltasNoBimestre());
    }
}
