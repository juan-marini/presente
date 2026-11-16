package com.github.juanmarini.ms.alunos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@Entity
@Table(name = "tb_aluno")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricula", nullable = false, unique = true, length = 20)
    private String matricula;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "turma", nullable = false, length = 20)
    private String turma;

    @Column(name = "escola", nullable = false, length = 120)
    private String escola;

    @Column(name = "nome_responsavel", nullable = false, length = 100)
    private String nomeResponsavel;

    @Column(name = "telefone_responsavel", nullable = false, length = 20)
    private String telefoneResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(name = "idioma_familia", nullable = false, length = 2)
    private IdiomaFamilia idiomaFamilia;

    @Column(name = "faltas_bimestre", nullable = false)
    private Integer faltasNoBimestre;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoAluno situacao;
}
