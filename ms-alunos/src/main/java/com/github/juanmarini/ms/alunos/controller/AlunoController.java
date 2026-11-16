package com.github.juanmarini.ms.alunos.controller;

import com.github.juanmarini.ms.alunos.dto.AlunoRequestDTO;
import com.github.juanmarini.ms.alunos.dto.AlunoResponseDTO;
import com.github.juanmarini.ms.alunos.dto.AlunoSituacaoRequestDTO;
import com.github.juanmarini.ms.alunos.entity.SituacaoAluno;
import com.github.juanmarini.ms.alunos.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/alunos")
@Tag(name = "Alunos", description = "Cadastro de alunos e situação escolar")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private Environment environment;

    @GetMapping
    @Operation(summary = "Lista os alunos, com filtro opcional por situação")
    public ResponseEntity<List<AlunoResponseDTO>> findAll(
            @RequestParam(required = false) SituacaoAluno situacao) {
        List<AlunoResponseDTO> list = alunoService.findAllAlunos(situacao);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um aluno pelo id")
    public ResponseEntity<AlunoResponseDTO> findById(@PathVariable @NotNull Long id) {
        log.info("ms-alunos atendendo GET /api/v1/alunos/{} na porta {}", id,
                environment.getProperty("local.server.port"));
        AlunoResponseDTO dto = alunoService.findAlunoById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(summary = "Cadastra um aluno com situação inicial REGULAR")
    public ResponseEntity<AlunoResponseDTO> create(@RequestBody @Valid AlunoRequestDTO requestDTO) {
        AlunoResponseDTO dto = alunoService.saveAluno(requestDTO);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados cadastrais de um aluno")
    public ResponseEntity<AlunoResponseDTO> update(@PathVariable @NotNull Long id,
                                                   @RequestBody @Valid AlunoRequestDTO requestDTO) {
        AlunoResponseDTO dto = alunoService.updateAluno(id, requestDTO);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um aluno que não esteja em busca ativa")
    public ResponseEntity<Void> delete(@PathVariable @NotNull Long id) {
        alunoService.deleteAluno(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/situacao")
    @Operation(summary = "Altera a situação do aluno (utilizado pelo ms-casos via OpenFeign)")
    public ResponseEntity<AlunoResponseDTO> alterarSituacao(@PathVariable @NotNull Long id,
                                                            @RequestBody @Valid AlunoSituacaoRequestDTO requestDTO) {
        log.info("ms-alunos alterando situação do aluno {} para {} na porta {}", id,
                requestDTO.getSituacao(), environment.getProperty("local.server.port"));
        AlunoResponseDTO dto = alunoService.alterarSituacao(id, requestDTO.getSituacao());
        return ResponseEntity.ok(dto);
    }
}
