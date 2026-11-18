package com.github.juanmarini.ms.alunos.service;

import com.github.juanmarini.ms.alunos.dto.AlunoResponseDTO;
import com.github.juanmarini.ms.alunos.entity.Aluno;
import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;
import com.github.juanmarini.ms.alunos.exceptions.AlunoEmBuscaAtivaException;
import com.github.juanmarini.ms.alunos.exceptions.DatabaseException;
import com.github.juanmarini.ms.alunos.exceptions.ResourceNotFoundException;
import com.github.juanmarini.ms.alunos.exceptions.SituacaoInvalidaException;
import com.github.juanmarini.ms.alunos.repository.AlunoRepository;
import com.github.juanmarini.ms.alunos.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AlunoService alunoService;

    private Long existingId;
    private Long nonExistingId;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = Long.MAX_VALUE;
        aluno = Factory.createAluno();
    }

    @Test
    void findAlunoByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Mockito.when(alunoRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> alunoService.findAlunoById(nonExistingId));

        Mockito.verify(alunoRepository).findById(nonExistingId);
    }

    @Test
    void updateAlunoShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Mockito.when(alunoRepository.existsById(nonExistingId)).thenReturn(false);

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> alunoService.updateAluno(nonExistingId, Factory.createAlunoRequestDTO()));

        Mockito.verify(alunoRepository, Mockito.never()).save(Mockito.any(Aluno.class));
    }

    @Test
    void saveAlunoShouldReturnAlunoWithSituacaoRegularWhenDataIsValid() {
        Mockito.when(alunoRepository.existsByMatricula(aluno.getMatricula())).thenReturn(false);
        Mockito.when(alunoRepository.save(Mockito.any(Aluno.class))).thenReturn(aluno);

        AlunoResponseDTO result = alunoService.saveAluno(Factory.createAlunoRequestDTO());

        Assertions.assertEquals(existingId, result.getId());
        Assertions.assertEquals(SituacaoAluno.REGULAR, result.getSituacao());
        Mockito.verify(alunoRepository).save(Mockito.any(Aluno.class));
    }

    @Test
    void saveAlunoShouldThrowDatabaseExceptionWhenMatriculaAlreadyExists() {
        Mockito.when(alunoRepository.existsByMatricula(aluno.getMatricula())).thenReturn(true);

        Assertions.assertThrows(DatabaseException.class,
                () -> alunoService.saveAluno(Factory.createAlunoRequestDTO()));

        Mockito.verify(alunoRepository, Mockito.never()).save(Mockito.any(Aluno.class));
    }

    @Test
    void deleteAlunoShouldThrowAlunoEmBuscaAtivaExceptionWhenAlunoIsInBuscaAtiva() {
        aluno.setSituacao(SituacaoAluno.EM_BUSCA_ATIVA);
        Mockito.when(alunoRepository.findById(existingId)).thenReturn(Optional.of(aluno));

        Assertions.assertThrows(AlunoEmBuscaAtivaException.class,
                () -> alunoService.deleteAluno(existingId));

        Mockito.verify(alunoRepository, Mockito.never()).deleteById(Mockito.anyLong());
    }

    @Test
    void alterarSituacaoShouldChangeToEmBuscaAtivaWhenAlunoIsRegular() {
        Mockito.when(alunoRepository.findById(existingId)).thenReturn(Optional.of(aluno));
        Mockito.when(alunoRepository.save(aluno)).thenReturn(aluno);

        AlunoResponseDTO result = alunoService.alterarSituacao(existingId, SituacaoAluno.EM_BUSCA_ATIVA);

        Assertions.assertEquals(SituacaoAluno.EM_BUSCA_ATIVA, result.getSituacao());
        Mockito.verify(alunoRepository).save(aluno);
    }

    @Test
    void alterarSituacaoShouldThrowSituacaoInvalidaExceptionWhenTransitionIsNotAllowed() {
        Mockito.when(alunoRepository.findById(existingId)).thenReturn(Optional.of(aluno));

        Assertions.assertThrows(SituacaoInvalidaException.class,
                () -> alunoService.alterarSituacao(existingId, SituacaoAluno.EVADIDO));

        Mockito.verify(alunoRepository, Mockito.never()).save(Mockito.any(Aluno.class));
    }
}
