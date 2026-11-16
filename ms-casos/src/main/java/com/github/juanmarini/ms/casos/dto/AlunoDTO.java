package com.github.juanmarini.ms.casos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AlunoDTO {

    private Long id;
    private String matricula;
    private String nome;
    private String turma;
    private String escola;
    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String idiomaFamilia;
    private String situacao;
}
