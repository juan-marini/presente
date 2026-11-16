package com.github.juanmarini.ms.casos.service;

import com.github.juanmarini.ms.casos.client.AlunoClient;
import com.github.juanmarini.ms.casos.dto.AlunoSituacaoRequestDTO;
import com.github.juanmarini.ms.casos.dto.CasoResponseDTO;
import com.github.juanmarini.ms.casos.entity.CasoBuscaAtiva;
import com.github.juanmarini.ms.casos.entity.RedeApoio;
import com.github.juanmarini.ms.casos.entity.StatusCaso;
import com.github.juanmarini.ms.casos.exceptions.CasoAtivoExistenteException;
import com.github.juanmarini.ms.casos.exceptions.ResourceNotFoundException;
import com.github.juanmarini.ms.casos.exceptions.TransicaoStatusInvalidaException;
import com.github.juanmarini.ms.casos.repository.CasoBuscaAtivaRepository;
import com.github.juanmarini.ms.casos.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CasoBuscaAtivaServiceTest {

    @Mock
    private CasoBuscaAtivaRepository casoRepository;

    @Mock
    private AlunoClient alunoClient;

    @InjectMocks
    private CasoBuscaAtivaService casoService;

    private Long existingId;
    private Long nonExistingId;
    private CasoBuscaAtiva caso;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = Long.MAX_VALUE;
        caso = Factory.createCaso();
    }

    @Test
    void deleteCasoShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Mockito.when(casoRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> casoService.deleteCaso(nonExistingId));

        Mockito.verify(casoRepository, Mockito.never()).deleteById(Mockito.anyLong());
    }

    @Test
    void saveCasoShouldSuggestEncaminhamentoAndChangeAlunoToEmBuscaAtivaWhenDataIsValid() {
        Mockito.when(alunoClient.buscarAluno(1L)).thenReturn(Factory.createAlunoDTO("REGULAR"));
        Mockito.when(casoRepository.existsByAlunoIdAndStatusIn(Mockito.eq(1L), Mockito.anyCollection()))
                .thenReturn(false);
        Mockito.when(casoRepository.save(Mockito.any(CasoBuscaAtiva.class))).thenReturn(caso);
        Mockito.when(alunoClient.alterarSituacao(Mockito.eq(1L), Mockito.any(AlunoSituacaoRequestDTO.class)))
                .thenReturn(Factory.createAlunoDTO("EM_BUSCA_ATIVA"));

        CasoResponseDTO result = casoService.saveCaso(Factory.createCasoRequestDTOSemEncaminhamento());

        ArgumentCaptor<AlunoSituacaoRequestDTO> captor = ArgumentCaptor.forClass(AlunoSituacaoRequestDTO.class);
        Mockito.verify(alunoClient).alterarSituacao(Mockito.eq(1L), captor.capture());
        Assertions.assertEquals("EM_BUSCA_ATIVA", captor.getValue().getSituacao());
        Assertions.assertEquals(RedeApoio.PASSE_LIVRE, result.getEncaminhamento());
        Assertions.assertEquals(StatusCaso.ABERTO, result.getStatus());
        Assertions.assertEquals("EM_BUSCA_ATIVA", result.getAluno().getSituacao());
    }

    @Test
    void saveCasoShouldThrowCasoAtivoExistenteExceptionWhenAlunoAlreadyHasActiveCaso() {
        Mockito.when(alunoClient.buscarAluno(1L)).thenReturn(Factory.createAlunoDTO("EM_BUSCA_ATIVA"));
        Mockito.when(casoRepository.existsByAlunoIdAndStatusIn(Mockito.eq(1L), Mockito.anyCollection()))
                .thenReturn(true);

        Assertions.assertThrows(CasoAtivoExistenteException.class,
                () -> casoService.saveCaso(Factory.createCasoRequestDTOSemEncaminhamento()));

        Mockito.verify(casoRepository, Mockito.never()).save(Mockito.any(CasoBuscaAtiva.class));
        Mockito.verify(alunoClient, Mockito.never()).alterarSituacao(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void alterarStatusShouldThrowTransicaoStatusInvalidaExceptionWhenTransitionIsNotAllowed() {
        Mockito.when(casoRepository.findById(existingId)).thenReturn(Optional.of(caso));

        Assertions.assertThrows(TransicaoStatusInvalidaException.class,
                () -> casoService.alterarStatus(existingId, StatusCaso.RESOLVIDO));

        Mockito.verifyNoInteractions(alunoClient);
    }

    @Test
    void alterarStatusShouldChangeAlunoToRetornouWhenCasoIsResolvido() {
        caso.setStatus(StatusCaso.ACOMPANHANDO);
        Mockito.when(casoRepository.findById(existingId)).thenReturn(Optional.of(caso));
        Mockito.when(casoRepository.save(caso)).thenReturn(caso);
        Mockito.when(alunoClient.alterarSituacao(Mockito.eq(1L), Mockito.any(AlunoSituacaoRequestDTO.class)))
                .thenReturn(Factory.createAlunoDTO("RETORNOU"));

        CasoResponseDTO result = casoService.alterarStatus(existingId, StatusCaso.RESOLVIDO);

        Assertions.assertEquals(StatusCaso.RESOLVIDO, result.getStatus());
        Assertions.assertNotNull(result.getDataEncerramento());
        Assertions.assertEquals("RETORNOU", result.getAluno().getSituacao());
        Mockito.verify(alunoClient).alterarSituacao(Mockito.eq(1L), Mockito.any(AlunoSituacaoRequestDTO.class));
    }
}
