package com.github.juanmarini.ms.casos.service;

import com.github.juanmarini.ms.casos.client.AlunoClient;
import com.github.juanmarini.ms.casos.dto.AlunoDTO;
import com.github.juanmarini.ms.casos.dto.AlunoSituacaoRequestDTO;
import com.github.juanmarini.ms.casos.dto.CasoRequestDTO;
import com.github.juanmarini.ms.casos.dto.CasoResponseDTO;
import com.github.juanmarini.ms.casos.dto.CasoUpdateRequestDTO;
import com.github.juanmarini.ms.casos.entity.CasoBuscaAtiva;
import com.github.juanmarini.ms.casos.entity.StatusCaso;
import com.github.juanmarini.ms.casos.exceptions.CasoAtivoExistenteException;
import com.github.juanmarini.ms.casos.exceptions.CasoEncerradoException;
import com.github.juanmarini.ms.casos.exceptions.ResourceNotFoundException;
import com.github.juanmarini.ms.casos.exceptions.SituacaoAlunoInvalidaException;
import com.github.juanmarini.ms.casos.exceptions.TransicaoStatusInvalidaException;
import com.github.juanmarini.ms.casos.repository.CasoBuscaAtivaRepository;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CasoBuscaAtivaService {

    @Autowired
    private CasoBuscaAtivaRepository casoRepository;

    @Autowired
    private AlunoClient alunoClient;

    @Transactional(readOnly = true)
    public List<CasoResponseDTO> findAllCasos(StatusCaso status, Long alunoId) {
        List<CasoBuscaAtiva> casos;
        if (status != null && alunoId != null) {
            casos = casoRepository.findByAlunoIdAndStatus(alunoId, status);
        } else if (status != null) {
            casos = casoRepository.findByStatus(status);
        } else if (alunoId != null) {
            casos = casoRepository.findByAlunoId(alunoId);
        } else {
            casos = casoRepository.findAll();
        }
        return casos.stream().map(CasoResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public CasoResponseDTO findCasoById(Long id) {
        CasoBuscaAtiva caso = buscarCaso(id);
        AlunoDTO aluno = buscarAluno(caso.getAlunoId());
        return new CasoResponseDTO(caso, aluno);
    }

    @Transactional(readOnly = true)
    public CasoResponseDTO findCasoByIdSemAluno(Long id, String mensagem) {
        CasoBuscaAtiva caso = buscarCaso(id);
        return new CasoResponseDTO(caso, mensagem);
    }

    @Transactional
    public CasoResponseDTO saveCaso(CasoRequestDTO requestDTO) {
        buscarAluno(requestDTO.getAlunoId());

        if (casoRepository.existsByAlunoIdAndStatusIn(requestDTO.getAlunoId(), StatusCaso.ativos())) {
            throw new CasoAtivoExistenteException(
                    String.format("Aluno id %d já possui um caso de busca ativa em andamento",
                            requestDTO.getAlunoId()));
        }

        CasoBuscaAtiva caso = new CasoBuscaAtiva();
        caso.setAlunoId(requestDTO.getAlunoId());
        caso.setMotivo(requestDTO.getMotivo());
        caso.setResumo(requestDTO.getResumo());
        caso.setAgenteResponsavel(requestDTO.getAgenteResponsavel());
        caso.setEncaminhamento(requestDTO.getEncaminhamento() != null
                ? requestDTO.getEncaminhamento()
                : requestDTO.getMotivo().encaminhamentoSugerido());
        caso.setPrioridade(requestDTO.getPrioridade() != null
                ? requestDTO.getPrioridade()
                : requestDTO.getMotivo().prioridadeSugerida());
        caso.setStatus(StatusCaso.ABERTO);
        caso.setDataAbertura(LocalDate.now());
        caso = casoRepository.save(caso);

        AlunoDTO aluno = alterarSituacaoAluno(caso.getAlunoId(), "EM_BUSCA_ATIVA");
        return new CasoResponseDTO(caso, aluno);
    }

    @Transactional
    public CasoResponseDTO updateCaso(Long id, CasoUpdateRequestDTO requestDTO) {
        try {
            CasoBuscaAtiva caso = casoRepository.getReferenceById(id);
            if (!caso.getStatus().isAtivo()) {
                throw new CasoEncerradoException(
                        String.format("Caso id %d já está %s e não pode ser alterado", id, caso.getStatus()));
            }
            copyDtoToCaso(requestDTO, caso);
            caso = casoRepository.save(caso);
            return new CasoResponseDTO(caso);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Caso não encontrado. ID: " + id);
        }
    }

    @Transactional
    public void deleteCaso(Long id) {
        CasoBuscaAtiva caso = buscarCaso(id);
        if (caso.getStatus().isAtivo()) {
            throw new CasoAtivoExistenteException(
                    String.format("Caso id %d está em andamento (%s). Somente casos encerrados podem ser excluídos",
                            id, caso.getStatus()));
        }
        casoRepository.deleteById(id);
    }

    @Transactional
    public CasoResponseDTO alterarStatus(Long id, StatusCaso novoStatus) {
        CasoBuscaAtiva caso = buscarCaso(id);

        if (!caso.getStatus().podeMudarPara(novoStatus)) {
            throw new TransicaoStatusInvalidaException(
                    String.format("Status do caso id %d não pode mudar de %s para %s",
                            id, caso.getStatus(), novoStatus));
        }

        caso.setStatus(novoStatus);
        if (!novoStatus.isAtivo()) {
            caso.setDataEncerramento(LocalDate.now());
        }
        caso = casoRepository.save(caso);

        AlunoDTO aluno = switch (novoStatus) {
            case RESOLVIDO -> alterarSituacaoAluno(caso.getAlunoId(), "RETORNOU");
            case ENCERRADO_SEM_RETORNO -> alterarSituacaoAluno(caso.getAlunoId(), "EVADIDO");
            default -> null;
        };
        return new CasoResponseDTO(caso, aluno);
    }

    private CasoBuscaAtiva buscarCaso(Long id) {
        return casoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Caso não encontrado. ID: " + id)
        );
    }

    private AlunoDTO buscarAluno(Long alunoId) {
        try {
            return alunoClient.buscarAluno(alunoId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Aluno não encontrado. ID: " + alunoId);
        }
    }

    private AlunoDTO alterarSituacaoAluno(Long alunoId, String situacao) {
        try {
            return alunoClient.alterarSituacao(alunoId, new AlunoSituacaoRequestDTO(situacao));
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Aluno não encontrado. ID: " + alunoId);
        } catch (FeignException.Conflict e) {
            throw new SituacaoAlunoInvalidaException(
                    String.format("A situação atual do aluno id %d não permite alterar para %s",
                            alunoId, situacao));
        }
    }

    private void copyDtoToCaso(CasoUpdateRequestDTO requestDTO, CasoBuscaAtiva caso) {
        caso.setMotivo(requestDTO.getMotivo());
        caso.setResumo(requestDTO.getResumo());
        caso.setEncaminhamento(requestDTO.getEncaminhamento());
        caso.setPrioridade(requestDTO.getPrioridade());
        caso.setAgenteResponsavel(requestDTO.getAgenteResponsavel());
    }
}
