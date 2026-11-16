package com.github.juanmarini.ms.casos.tests;

import com.github.juanmarini.ms.casos.dto.AlunoDTO;
import com.github.juanmarini.ms.casos.dto.CasoRequestDTO;
import com.github.juanmarini.ms.casos.entity.CasoBuscaAtiva;
import com.github.juanmarini.ms.casos.entity.MotivoFalta;
import com.github.juanmarini.ms.casos.entity.Prioridade;
import com.github.juanmarini.ms.casos.entity.RedeApoio;
import com.github.juanmarini.ms.casos.entity.StatusCaso;

import java.time.LocalDate;

public class Factory {

    public static CasoBuscaAtiva createCaso() {
        return new CasoBuscaAtiva(
                1L,
                1L,
                MotivoFalta.TRANSPORTE,
                "Família informou que o aluno não tem dinheiro para a condução.",
                RedeApoio.PASSE_LIVRE,
                Prioridade.NORMAL,
                "Mariana Teixeira",
                StatusCaso.ABERTO,
                LocalDate.of(2026, 11, 10),
                null
        );
    }

    public static CasoRequestDTO createCasoRequestDTOSemEncaminhamento() {
        return new CasoRequestDTO(
                1L,
                MotivoFalta.TRANSPORTE,
                "Família informou que o aluno não tem dinheiro para a condução.",
                null,
                null,
                "Mariana Teixeira"
        );
    }

    public static AlunoDTO createAlunoDTO(String situacao) {
        return new AlunoDTO(1L, "2026EM0001", "Gabriel Souza Lima", "1A-EM",
                "E.E. Prof. Maria José", "Cláudia Souza", "+5511987650001", "PT", situacao);
    }
}
