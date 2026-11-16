package com.github.juanmarini.ms.alunos.entity;

public enum SituacaoAluno {

    REGULAR,
    EM_BUSCA_ATIVA,
    RETORNOU,
    EVADIDO,
    TRANSFERIDO;

    public boolean podeMudarPara(SituacaoAluno novaSituacao) {
        return switch (this) {
            case REGULAR -> novaSituacao == EM_BUSCA_ATIVA;
            case EM_BUSCA_ATIVA -> novaSituacao == RETORNOU
                    || novaSituacao == EVADIDO
                    || novaSituacao == TRANSFERIDO;
            case RETORNOU, EVADIDO -> novaSituacao == EM_BUSCA_ATIVA;
            case TRANSFERIDO -> false;
        };
    }
}
