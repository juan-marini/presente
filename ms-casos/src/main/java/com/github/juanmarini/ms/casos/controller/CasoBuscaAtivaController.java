package com.github.juanmarini.ms.casos.controller;

import com.github.juanmarini.ms.casos.dto.CasoRequestDTO;
import com.github.juanmarini.ms.casos.dto.CasoResponseDTO;
import com.github.juanmarini.ms.casos.dto.CasoStatusRequestDTO;
import com.github.juanmarini.ms.casos.dto.CasoUpdateRequestDTO;
import com.github.juanmarini.ms.casos.entity.StatusCaso;
import com.github.juanmarini.ms.casos.exceptions.ServicoIndisponivelException;
import com.github.juanmarini.ms.casos.service.CasoBuscaAtivaService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/casos")
@Tag(name = "Casos de busca ativa", description = "Casos abertos para alunos com risco de evasão")
public class CasoBuscaAtivaController {

    @Autowired
    private CasoBuscaAtivaService casoService;

    @GetMapping
    @Operation(summary = "Lista os casos, com filtros opcionais por status e por aluno")
    public ResponseEntity<List<CasoResponseDTO>> findAll(@RequestParam(required = false) StatusCaso status,
                                                         @RequestParam(required = false) Long alunoId) {
        List<CasoResponseDTO> list = casoService.findAllCasos(status, alunoId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "alunos", fallbackMethod = "fallbackFindById")
    @Operation(summary = "Busca um caso pelo id, com os dados do aluno obtidos do ms-alunos")
    public ResponseEntity<CasoResponseDTO> findById(@PathVariable @NotNull Long id) {
        CasoResponseDTO dto = casoService.findCasoById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @CircuitBreaker(name = "alunos", fallbackMethod = "fallbackCreate")
    @Operation(summary = "Abre um caso de busca ativa e coloca o aluno EM_BUSCA_ATIVA no ms-alunos")
    public ResponseEntity<CasoResponseDTO> create(@RequestBody @Valid CasoRequestDTO requestDTO) {
        CasoResponseDTO dto = casoService.saveCaso(requestDTO);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza motivo, resumo, encaminhamento, prioridade e agente de um caso em andamento")
    public ResponseEntity<CasoResponseDTO> update(@PathVariable @NotNull Long id,
                                                  @RequestBody @Valid CasoUpdateRequestDTO requestDTO) {
        CasoResponseDTO dto = casoService.updateCaso(id, requestDTO);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um caso encerrado")
    public ResponseEntity<Void> delete(@PathVariable @NotNull Long id) {
        casoService.deleteCaso(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @CircuitBreaker(name = "alunos", fallbackMethod = "fallbackAlterarStatus")
    @Operation(summary = "Altera o status do caso. RESOLVIDO e ENCERRADO_SEM_RETORNO atualizam a situação do aluno no ms-alunos")
    public ResponseEntity<CasoResponseDTO> alterarStatus(@PathVariable @NotNull Long id,
                                                         @RequestBody @Valid CasoStatusRequestDTO requestDTO) {
        CasoResponseDTO dto = casoService.alterarStatus(id, requestDTO.getStatus());
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<CasoResponseDTO> fallbackFindById(Long id, Throwable e) {
        relancarSeNaoForFalhaDeComunicacao(e);
        log.error("Falha ao consultar o ms-alunos para o caso {}. Ativando fallback. Erro: {}", id, e.getMessage());
        CasoResponseDTO dto = casoService.findCasoByIdSemAluno(id,
                "Dados do aluno temporariamente indisponíveis");
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<CasoResponseDTO> fallbackCreate(CasoRequestDTO requestDTO, Throwable e) {
        relancarSeNaoForFalhaDeComunicacao(e);
        log.error("Falha ao comunicar com o ms-alunos ao abrir caso para o aluno {}. Ativando fallback. Erro: {}",
                requestDTO.getAlunoId(), e.getMessage());
        throw new ServicoIndisponivelException(
                "Serviço de alunos indisponível. O caso não foi registrado, tente novamente em instantes.");
    }

    public ResponseEntity<CasoResponseDTO> fallbackAlterarStatus(Long id, CasoStatusRequestDTO requestDTO,
                                                                 Throwable e) {
        relancarSeNaoForFalhaDeComunicacao(e);
        log.error("Falha ao comunicar com o ms-alunos ao alterar o status do caso {}. Ativando fallback. Erro: {}",
                id, e.getMessage());
        throw new ServicoIndisponivelException(
                "Serviço de alunos indisponível. O status do caso não foi alterado, tente novamente em instantes.");
    }

    private void relancarSeNaoForFalhaDeComunicacao(Throwable e) {
        if (e instanceof FeignException || e instanceof CallNotPermittedException) {
            return;
        }
        if (e instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException(e);
    }
}
