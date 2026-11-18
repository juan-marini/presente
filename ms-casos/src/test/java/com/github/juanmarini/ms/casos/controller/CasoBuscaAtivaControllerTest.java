package com.github.juanmarini.ms.casos.controller;

import com.github.juanmarini.ms.casos.client.AlunoClient;
import com.github.juanmarini.ms.casos.dto.CasoRequestDTO;
import com.github.juanmarini.ms.casos.dto.CasoResponseDTO;
import com.github.juanmarini.ms.casos.dto.CasoStatusRequestDTO;
import com.github.juanmarini.ms.casos.dto.CasoUpdateRequestDTO;
import com.github.juanmarini.ms.casos.entity.CasoBuscaAtiva;
import com.github.juanmarini.ms.casos.entity.MotivoFalta;
import com.github.juanmarini.ms.casos.entity.Prioridade;
import com.github.juanmarini.ms.casos.entity.RedeApoio;
import com.github.juanmarini.ms.casos.entity.StatusCaso;
import com.github.juanmarini.ms.casos.exceptions.CasoAtivoExistenteException;
import com.github.juanmarini.ms.casos.exceptions.ResourceNotFoundException;
import com.github.juanmarini.ms.casos.exceptions.TransicaoStatusInvalidaException;
import com.github.juanmarini.ms.casos.service.CasoBuscaAtivaService;
import com.github.juanmarini.ms.casos.tests.Factory;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CasoBuscaAtivaController.class)
class CasoBuscaAtivaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CasoBuscaAtivaService casoService;

    @MockitoBean
    private AlunoClient alunoClient;

    private Long existingId;
    private Long nonExistingId;
    private CasoBuscaAtiva caso;
    private CasoRequestDTO requestDTO;
    private CasoUpdateRequestDTO updateRequestDTO;
    private CasoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = Long.MAX_VALUE;
        caso = Factory.createCaso();
        requestDTO = Factory.createCasoRequestDTOSemEncaminhamento();
        updateRequestDTO = new CasoUpdateRequestDTO(MotivoFalta.SAUDE,
                "Após contato, a família relatou que o aluno está em tratamento médico.",
                RedeApoio.UBS, Prioridade.ALTA, "Mariana Teixeira");
        responseDTO = new CasoResponseDTO(caso, Factory.createAlunoDTO("EM_BUSCA_ATIVA"));
    }

    @Test
    void findAllShouldReturnListOfCasosWhenCalled() throws Exception {
        Mockito.when(casoService.findAllCasos(null, null)).thenReturn(List.of(new CasoResponseDTO(caso)));

        mockMvc.perform(get("/api/v1/casos").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(existingId))
                .andExpect(jsonPath("$[0].status").value("ABERTO"));

        Mockito.verify(casoService).findAllCasos(null, null);
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void findByIdShouldReturnCasoWithAlunoWhenIdExists() throws Exception {
        Mockito.when(casoService.findCasoById(existingId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/casos/{id}", existingId).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.dataAbertura").value(caso.getDataAbertura().toString()))
                .andExpect(jsonPath("$.aluno.situacao").value("EM_BUSCA_ATIVA"));

        Mockito.verify(casoService).findCasoById(existingId);
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        Mockito.when(casoService.findCasoById(nonExistingId))
                .thenThrow(new ResourceNotFoundException("Caso não encontrado. ID: " + nonExistingId));

        mockMvc.perform(get("/api/v1/casos/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Caso não encontrado. ID: " + nonExistingId));

        Mockito.verify(casoService).findCasoById(nonExistingId);
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void createShouldReturnCreatedWithLocationWhenDataIsValid() throws Exception {
        Mockito.when(casoService.saveCaso(ArgumentMatchers.any(CasoRequestDTO.class))).thenReturn(responseDTO);
        String jsonBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/v1/casos")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/api/v1/casos/" + existingId))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.encaminhamento").value("PASSE_LIVRE"));

        Mockito.verify(casoService).saveCaso(ArgumentMatchers.any(CasoRequestDTO.class));
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void createShouldReturnBadRequestWhenDataIsInvalid() throws Exception {
        CasoRequestDTO invalido = new CasoRequestDTO(null, null, "curto", null, null, "");
        String jsonBody = objectMapper.writeValueAsString(invalido);

        mockMvc.perform(post("/api/v1/casos")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors").isArray());

        Mockito.verifyNoInteractions(casoService);
    }

    @Test
    void createShouldReturnConflictWhenAlunoAlreadyHasActiveCaso() throws Exception {
        Mockito.when(casoService.saveCaso(ArgumentMatchers.any(CasoRequestDTO.class)))
                .thenThrow(new CasoAtivoExistenteException("Aluno id 1 já possui um caso de busca ativa em andamento"));
        String jsonBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/api/v1/casos")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());

        Mockito.verify(casoService).saveCaso(ArgumentMatchers.any(CasoRequestDTO.class));
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void updateShouldReturnOkWhenIdExists() throws Exception {
        Mockito.when(casoService.updateCaso(ArgumentMatchers.eq(existingId), ArgumentMatchers.any(CasoUpdateRequestDTO.class)))
                .thenReturn(new CasoResponseDTO(caso));
        String jsonBody = objectMapper.writeValueAsString(updateRequestDTO);

        mockMvc.perform(put("/api/v1/casos/{id}", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId));

        Mockito.verify(casoService).updateCaso(ArgumentMatchers.eq(existingId), ArgumentMatchers.any(CasoUpdateRequestDTO.class));
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void deleteShouldReturnNoContentWhenIdExists() throws Exception {
        Mockito.doNothing().when(casoService).deleteCaso(existingId);

        mockMvc.perform(delete("/api/v1/casos/{id}", existingId))
                .andDo(print())
                .andExpect(status().isNoContent());

        Mockito.verify(casoService).deleteCaso(existingId);
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Caso não encontrado. ID: " + nonExistingId))
                .when(casoService).deleteCaso(nonExistingId);

        mockMvc.perform(delete("/api/v1/casos/{id}", nonExistingId))
                .andDo(print())
                .andExpect(status().isNotFound());

        Mockito.verify(casoService).deleteCaso(nonExistingId);
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void alterarStatusShouldReturnOkWhenTransitionIsAllowed() throws Exception {
        caso.setStatus(StatusCaso.EM_CONTATO);
        Mockito.when(casoService.alterarStatus(existingId, StatusCaso.EM_CONTATO))
                .thenReturn(new CasoResponseDTO(caso));
        String jsonBody = objectMapper.writeValueAsString(new CasoStatusRequestDTO(StatusCaso.EM_CONTATO));

        mockMvc.perform(patch("/api/v1/casos/{id}/status", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_CONTATO"));

        Mockito.verify(casoService).alterarStatus(existingId, StatusCaso.EM_CONTATO);
        Mockito.verifyNoMoreInteractions(casoService);
    }

    @Test
    void alterarStatusShouldReturnConflictWhenTransitionIsNotAllowed() throws Exception {
        Mockito.when(casoService.alterarStatus(existingId, StatusCaso.RESOLVIDO))
                .thenThrow(new TransicaoStatusInvalidaException("Status do caso id 1 não pode mudar de ABERTO para RESOLVIDO"));
        String jsonBody = objectMapper.writeValueAsString(new CasoStatusRequestDTO(StatusCaso.RESOLVIDO));

        mockMvc.perform(patch("/api/v1/casos/{id}/status", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());

        Mockito.verify(casoService).alterarStatus(existingId, StatusCaso.RESOLVIDO);
        Mockito.verifyNoMoreInteractions(casoService);
    }
}
