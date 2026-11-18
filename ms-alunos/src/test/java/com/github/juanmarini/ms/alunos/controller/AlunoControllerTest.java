package com.github.juanmarini.ms.alunos.controller;

import com.github.juanmarini.ms.alunos.dto.AlunoRequestDTO;
import com.github.juanmarini.ms.alunos.dto.AlunoResponseDTO;
import com.github.juanmarini.ms.alunos.dto.AlunoSituacaoRequestDTO;
import com.github.juanmarini.ms.alunos.entity.Aluno;
import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;
import com.github.juanmarini.ms.alunos.exceptions.AlunoEmBuscaAtivaException;
import com.github.juanmarini.ms.alunos.exceptions.ResourceNotFoundException;
import com.github.juanmarini.ms.alunos.exceptions.SituacaoInvalidaException;
import com.github.juanmarini.ms.alunos.service.AlunoService;
import com.github.juanmarini.ms.alunos.tests.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlunoController.class)
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlunoService alunoService;

    private Long existingId;
    private Long nonExistingId;
    private AlunoRequestDTO requestDTO;
    private AlunoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = Long.MAX_VALUE;
        Aluno aluno = Factory.createAluno();
        requestDTO = Factory.createAlunoRequestDTO();
        responseDTO = new AlunoResponseDTO(aluno);
    }

    @Test
    void findAllShouldReturnListOfAlunosWhenCalled() throws Exception {
        Mockito.when(alunoService.findAllAlunos(null)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/alunos").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(existingId))
                .andExpect(jsonPath("$[0].matricula").value(responseDTO.getMatricula()));

        Mockito.verify(alunoService).findAllAlunos(null);
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void findByIdShouldReturnAlunoWhenIdExists() throws Exception {
        Mockito.when(alunoService.findAlunoById(existingId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/alunos/{id}", existingId).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.nome").value(responseDTO.getNome()))
                .andExpect(jsonPath("$.dataNascimento").value(responseDTO.getDataNascimento().toString()))
                .andExpect(jsonPath("$.situacao").value("REGULAR"));

        Mockito.verify(alunoService).findAlunoById(existingId);
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        Mockito.when(alunoService.findAlunoById(nonExistingId))
                .thenThrow(new ResourceNotFoundException("Aluno não encontrado. ID: " + nonExistingId));

        mockMvc.perform(get("/api/v1/alunos/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Aluno não encontrado. ID: " + nonExistingId));

        Mockito.verify(alunoService).findAlunoById(nonExistingId);
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void findByIdShouldReturnBadRequestWhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/alunos/{id}", "abc").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(alunoService);
    }

    @Test
    void createShouldReturnCreatedWithLocationWhenDataIsValid() throws Exception {
        Mockito.when(alunoService.saveAluno(ArgumentMatchers.any(AlunoRequestDTO.class))).thenReturn(responseDTO);
        String jsonBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/v1/alunos")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/api/v1/alunos/" + existingId))
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.situacao").value("REGULAR"));

        Mockito.verify(alunoService).saveAluno(ArgumentMatchers.any(AlunoRequestDTO.class));
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void createShouldReturnBadRequestWhenDataIsInvalid() throws Exception {
        AlunoRequestDTO invalido = new AlunoRequestDTO("", "", null, "", "", "", "11999", null, -1);
        String jsonBody = objectMapper.writeValueAsString(invalido);

        mockMvc.perform(post("/api/v1/alunos")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors").isArray());

        Mockito.verifyNoInteractions(alunoService);
    }

    @Test
    void updateShouldReturnOkWhenIdExists() throws Exception {
        Mockito.when(alunoService.updateAluno(ArgumentMatchers.eq(existingId), ArgumentMatchers.any(AlunoRequestDTO.class)))
                .thenReturn(responseDTO);
        String jsonBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(put("/api/v1/alunos/{id}", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId));

        Mockito.verify(alunoService).updateAluno(ArgumentMatchers.eq(existingId), ArgumentMatchers.any(AlunoRequestDTO.class));
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        Mockito.when(alunoService.updateAluno(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any(AlunoRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Aluno não encontrado. ID: " + nonExistingId));
        String jsonBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(put("/api/v1/alunos/{id}", nonExistingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        Mockito.verify(alunoService).updateAluno(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any(AlunoRequestDTO.class));
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void deleteShouldReturnNoContentWhenIdExists() throws Exception {
        Mockito.doNothing().when(alunoService).deleteAluno(existingId);

        mockMvc.perform(delete("/api/v1/alunos/{id}", existingId))
                .andDo(print())
                .andExpect(status().isNoContent());

        Mockito.verify(alunoService).deleteAluno(existingId);
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Aluno não encontrado. ID: " + nonExistingId))
                .when(alunoService).deleteAluno(nonExistingId);

        mockMvc.perform(delete("/api/v1/alunos/{id}", nonExistingId))
                .andDo(print())
                .andExpect(status().isNotFound());

        Mockito.verify(alunoService).deleteAluno(nonExistingId);
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void deleteShouldReturnConflictWhenAlunoIsInBuscaAtiva() throws Exception {
        Mockito.doThrow(new AlunoEmBuscaAtivaException("Aluno id 1 está em busca ativa e não pode ser excluído"))
                .when(alunoService).deleteAluno(existingId);

        mockMvc.perform(delete("/api/v1/alunos/{id}", existingId))
                .andDo(print())
                .andExpect(status().isConflict());

        Mockito.verify(alunoService).deleteAluno(existingId);
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void alterarSituacaoShouldReturnOkWhenTransitionIsAllowed() throws Exception {
        Aluno aluno = Factory.createAluno();
        aluno.setSituacao(SituacaoAluno.EM_BUSCA_ATIVA);
        Mockito.when(alunoService.alterarSituacao(existingId, SituacaoAluno.EM_BUSCA_ATIVA))
                .thenReturn(new AlunoResponseDTO(aluno));
        String jsonBody = objectMapper.writeValueAsString(new AlunoSituacaoRequestDTO(SituacaoAluno.EM_BUSCA_ATIVA));

        mockMvc.perform(put("/api/v1/alunos/{id}/situacao", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_BUSCA_ATIVA"));

        Mockito.verify(alunoService).alterarSituacao(existingId, SituacaoAluno.EM_BUSCA_ATIVA);
        Mockito.verifyNoMoreInteractions(alunoService);
    }

    @Test
    void alterarSituacaoShouldReturnConflictWhenTransitionIsNotAllowed() throws Exception {
        Mockito.when(alunoService.alterarSituacao(existingId, SituacaoAluno.RETORNOU))
                .thenThrow(new SituacaoInvalidaException("Situação do aluno id 1 não pode mudar de REGULAR para RETORNOU"));
        String jsonBody = objectMapper.writeValueAsString(new AlunoSituacaoRequestDTO(SituacaoAluno.RETORNOU));

        mockMvc.perform(put("/api/v1/alunos/{id}/situacao", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());

        Mockito.verify(alunoService).alterarSituacao(existingId, SituacaoAluno.RETORNOU);
        Mockito.verifyNoMoreInteractions(alunoService);
    }
}
