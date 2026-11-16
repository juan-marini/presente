package com.github.juanmarini.ms.casos.client;

import com.github.juanmarini.ms.casos.dto.AlunoDTO;
import com.github.juanmarini.ms.casos.dto.AlunoSituacaoRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("ms-alunos")
public interface AlunoClient {

    @RequestMapping(method = RequestMethod.GET,
            value = "/api/v1/alunos/{alunoId}")
    AlunoDTO buscarAluno(@PathVariable Long alunoId);

    @RequestMapping(method = RequestMethod.PUT,
            value = "/api/v1/alunos/{alunoId}/situacao")
    AlunoDTO alterarSituacao(@PathVariable Long alunoId,
                             @RequestBody AlunoSituacaoRequestDTO requestDTO);
}
