CREATE TABLE IF NOT EXISTS tb_caso_busca_ativa (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aluno_id BIGINT NOT NULL,
    motivo VARCHAR(30) NOT NULL,
    resumo VARCHAR(500) NOT NULL,
    encaminhamento VARCHAR(30) NOT NULL,
    prioridade VARCHAR(10) NOT NULL,
    agente_responsavel VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    data_abertura DATE NOT NULL,
    data_encerramento DATE NULL,
    CONSTRAINT pk_tb_caso_busca_ativa PRIMARY KEY (id)
);

CREATE INDEX idx_caso_aluno_status ON tb_caso_busca_ativa (aluno_id, status);
