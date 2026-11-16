package com.github.juanmarini.ms.casos.dto;

import com.github.juanmarini.ms.casos.entity.MotivoFalta;
import com.github.juanmarini.ms.casos.entity.Prioridade;
import com.github.juanmarini.ms.casos.entity.RedeApoio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CasoUpdateRequestDTO {

    @NotNull(message = "O motivo da falta é obrigatório")
    @Schema(example = "SAUDE")
    private MotivoFalta motivo;

    @NotBlank(message = "O resumo do caso é obrigatório")
    @Size(min = 10, max = 500, message = "O resumo deve ter entre 10 e 500 caracteres")
    @Schema(example = "Após contato, a família relatou que o aluno está em tratamento médico.")
    private String resumo;

    @NotNull(message = "O encaminhamento é obrigatório")
    @Schema(example = "UBS")
    private RedeApoio encaminhamento;

    @NotNull(message = "A prioridade é obrigatória")
    @Schema(example = "ALTA")
    private Prioridade prioridade;

    @NotBlank(message = "O agente responsável é obrigatório")
    @Size(min = 3, max = 100, message = "O agente responsável deve ter entre 3 e 100 caracteres")
    @Schema(example = "Mariana Teixeira")
    private String agenteResponsavel;
}
