package com.github.juanmarini.ms.casos.dto;

import com.github.juanmarini.ms.casos.entity.CasoBuscaAtiva;
import com.github.juanmarini.ms.casos.entity.MotivoFalta;
import com.github.juanmarini.ms.casos.entity.Prioridade;
import com.github.juanmarini.ms.casos.entity.RedeApoio;
import com.github.juanmarini.ms.casos.entity.StatusCaso;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CasoResponseDTO {

    private Long id;
    private Long alunoId;
    private MotivoFalta motivo;
    private String resumo;
    private RedeApoio encaminhamento;
    private Prioridade prioridade;
    private String agenteResponsavel;
    private StatusCaso status;
    private LocalDate dataAbertura;
    private LocalDate dataEncerramento;
    private AlunoDTO aluno;
    private String mensagem;

    public CasoResponseDTO(CasoBuscaAtiva caso) {
        id = caso.getId();
        alunoId = caso.getAlunoId();
        motivo = caso.getMotivo();
        resumo = caso.getResumo();
        encaminhamento = caso.getEncaminhamento();
        prioridade = caso.getPrioridade();
        agenteResponsavel = caso.getAgenteResponsavel();
        status = caso.getStatus();
        dataAbertura = caso.getDataAbertura();
        dataEncerramento = caso.getDataEncerramento();
    }

    public CasoResponseDTO(CasoBuscaAtiva caso, AlunoDTO aluno) {
        this(caso);
        this.aluno = aluno;
    }

    public CasoResponseDTO(CasoBuscaAtiva caso, String mensagem) {
        this(caso);
        this.mensagem = mensagem;
    }
}
