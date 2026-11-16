package com.github.juanmarini.ms.casos.entity;

public enum MotivoFalta {

    TRABALHO,
    TRANSPORTE,
    SAUDE,
    BULLYING,
    CUIDADO_FAMILIAR,
    DESMOTIVACAO,
    GRAVIDEZ,
    MUDANCA,
    VIOLACAO_DIREITOS,
    NAO_IDENTIFICADO;

    public RedeApoio encaminhamentoSugerido() {
        return switch (this) {
            case TRANSPORTE -> RedeApoio.PASSE_LIVRE;
            case SAUDE, GRAVIDEZ -> RedeApoio.UBS;
            case TRABALHO, CUIDADO_FAMILIAR -> RedeApoio.CRAS;
            case VIOLACAO_DIREITOS -> RedeApoio.CONSELHO_TUTELAR;
            case BULLYING, DESMOTIVACAO, MUDANCA, NAO_IDENTIFICADO -> RedeApoio.ORIENTACAO_ESCOLAR;
        };
    }

    public Prioridade prioridadeSugerida() {
        return switch (this) {
            case VIOLACAO_DIREITOS, BULLYING -> Prioridade.URGENTE;
            case SAUDE, GRAVIDEZ -> Prioridade.ALTA;
            default -> Prioridade.NORMAL;
        };
    }
}
