package com.github.juanmarini.ms.casos.dto;

import com.github.juanmarini.ms.casos.entity.StatusCaso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CasoStatusRequestDTO {

    @NotNull(message = "O status é obrigatório")
    @Schema(example = "EM_CONTATO")
    private StatusCaso status;
}
