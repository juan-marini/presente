package com.github.juanmarini.ms.casos.entity;

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
@Table(name = "tb_caso_busca_ativa")
public class CasoBuscaAtiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoFalta motivo;

    @Column(name = "resumo", nullable = false, length = 500)
    private String resumo;

    @Enumerated(EnumType.STRING)
    @Column(name = "encaminhamento", nullable = false, length = 30)
    private RedeApoio encaminhamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false, length = 10)
    private Prioridade prioridade;

    @Column(name = "agente_responsavel", nullable = false, length = 100)
    private String agenteResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusCaso status;

    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura;

    @Column(name = "data_encerramento")
    private LocalDate dataEncerramento;
}
