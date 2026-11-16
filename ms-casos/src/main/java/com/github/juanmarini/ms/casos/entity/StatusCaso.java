package com.github.juanmarini.ms.casos.entity;

import java.util.List;

public enum StatusCaso {

    ABERTO,
    EM_CONTATO,
    ENCAMINHADO,
    ACOMPANHANDO,
    RESOLVIDO,
    ENCERRADO_SEM_RETORNO;

    public boolean isAtivo() {
        return this != RESOLVIDO && this != ENCERRADO_SEM_RETORNO;
    }

    public boolean podeMudarPara(StatusCaso novoStatus) {
        if (!isAtivo()) {
            return false;
        }
        if (novoStatus == ENCERRADO_SEM_RETORNO) {
            return true;
        }
        return switch (this) {
            case ABERTO -> novoStatus == EM_CONTATO;
            case EM_CONTATO -> novoStatus == ENCAMINHADO;
            case ENCAMINHADO -> novoStatus == ACOMPANHANDO;
            case ACOMPANHANDO -> novoStatus == RESOLVIDO;
            default -> false;
        };
    }

    public static List<StatusCaso> ativos() {
        return List.of(ABERTO, EM_CONTATO, ENCAMINHADO, ACOMPANHANDO);
    }
}
