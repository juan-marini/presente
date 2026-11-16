CREATE TABLE IF NOT EXISTS tb_aluno (
    id BIGINT NOT NULL AUTO_INCREMENT,
    matricula VARCHAR(20) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    turma VARCHAR(20) NOT NULL,
    escola VARCHAR(120) NOT NULL,
    nome_responsavel VARCHAR(100) NOT NULL,
    telefone_responsavel VARCHAR(20) NOT NULL,
    idioma_familia VARCHAR(2) NOT NULL,
    faltas_bimestre INT NOT NULL,
    situacao VARCHAR(20) NOT NULL,
    CONSTRAINT pk_tb_aluno PRIMARY KEY (id),
    CONSTRAINT uk_tb_aluno_matricula UNIQUE (matricula)
);
