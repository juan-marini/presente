package com.github.juanmarini.ms.casos.dto;

import com.github.juanmarini.ms.casos.entity.MotivoFalta;
import com.github.juanmarini.ms.casos.entity.Prioridade;
import com.github.juanmarini.ms.casos.entity.RedeApoio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CasoRequestDTO {

    @NotNull(message = "O id do aluno é obrigatório")
    @Positive(message = "O id do aluno deve ser um número positivo")
    @Schema(example = "1")
    private Long alunoId;

    @NotNull(message = "O motivo da falta é obrigatório")
    @Schema(example = "TRANSPORTE")
    private MotivoFalta motivo;

    @NotBlank(message = "O resumo do caso é obrigatório")
    @Size(min = 10, max = 500, message = "O resumo deve ter entre 10 e 500 caracteres")
    @Schema(example = "Família informou que o aluno não tem dinheiro para a condução desde o início do mês.")
    private String resumo;

    @Schema(example = "PASSE_LIVRE", description = "Opcional. Quando não informado, é sugerido a partir do motivo")
    private RedeApoio encaminhamento;

    @Schema(example = "NORMAL", description = "Opcional. Quando não informada, é definida a partir do motivo")
    private Prioridade prioridade;

    @NotBlank(message = "O agente responsável é obrigatório")
    @Size(min = 3, max = 100, message = "O agente responsável deve ter entre 3 e 100 caracteres")
    @Schema(example = "Mariana Teixeira")
    private String agenteResponsavel;
}
