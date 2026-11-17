# Presente! — Microsserviços de Busca Ativa Escolar

Global Solution 2026.2 — **Microservice and Web Engineering & IT Services**
Prof.ª Aparecida F. Castello Rosa — 3º ano de Sistemas de Informação — FIAP

## Integrantes

| Nome | RM |
|---|---|
| Juan Marini | 556678 |
| Samuel Okuma | 555370 |
| Arthur Menon | 555918 |

## O problema

A evasão escolar ainda é um dos maiores problemas da educação pública brasileira, principalmente no Ensino Médio. Antes de abandonar a escola, o aluno costuma dar sinais: as faltas aumentam e a família deixa de ser encontrada. Os motivos variam: falta de dinheiro para a condução, trabalho precoce, cuidado de irmãos, saúde, gravidez, bullying, mudança de endereço. Muitas famílias, inclusive de imigrantes que não falam português, não são alcançadas pelos canais tradicionais da escola.

A **busca ativa escolar** é o processo de identificar esses alunos, entender o motivo das faltas e encaminhar a família para a rede de apoio certa (CRAS, UBS, Conselho Tutelar, Passe Livre, orientação escolar). Hoje esse trabalho costuma ser feito em planilhas e cadernos, sem histórico e sem integração.

Objetivos de Desenvolvimento Sustentável relacionados: **ODS 4** (Educação de qualidade) e **ODS 10** (Redução das desigualdades).

## A solução

O **Presente!** é uma plataforma CitizenTech de apoio à busca ativa escolar. Este repositório contém o **núcleo de dados da plataforma**, construído em arquitetura de microsserviços com comunicação síncrona:

- **ms-alunos**: cadastro dos alunos e da **situação escolar** de cada um (`REGULAR`, `EM_BUSCA_ATIVA`, `RETORNOU`, `EVADIDO`, `TRANSFERIDO`), com o idioma da família para o atendimento multilíngue.
- **ms-casos**: os **casos de busca ativa** abertos para cada aluno. Registra o motivo das faltas, o resumo do atendimento, o encaminhamento para a rede de apoio, a prioridade, o agente responsável e o **status do caso** (`ABERTO`, `EM_CONTATO`, `ENCAMINHADO`, `ACOMPANHANDO`, `RESOLVIDO`, `ENCERRADO_SEM_RETORNO`).

Os dois serviços conversam por **OpenFeign**. O andamento do caso altera a situação do aluno no outro serviço:

| Ação no ms-casos | Chamada síncrona ao ms-alunos | Resultado |
|---|---|---|
| Abrir caso (`POST /api/v1/casos`) | `GET /api/v1/alunos/{id}` e `PUT /api/v1/alunos/{id}/situacao` | aluno passa para `EM_BUSCA_ATIVA` |
| Status `RESOLVIDO` (`PATCH /api/v1/casos/{id}/status`) | `PUT /api/v1/alunos/{id}/situacao` | aluno passa para `RETORNOU` |
| Status `ENCERRADO_SEM_RETORNO` | `PUT /api/v1/alunos/{id}/situacao` | aluno passa para `EVADIDO` |
| Consultar caso (`GET /api/v1/casos/{id}`) | `GET /api/v1/alunos/{id}` | resposta traz os dados do aluno |

Regras de negócio:

- O encaminhamento é sugerido a partir do motivo quando não é informado: TRANSPORTE → PASSE_LIVRE, SAUDE/GRAVIDEZ → UBS, TRABALHO/CUIDADO_FAMILIAR → CRAS, VIOLACAO_DIREITOS → CONSELHO_TUTELAR e os demais → ORIENTACAO_ESCOLAR.
- A prioridade também é automática: VIOLACAO_DIREITOS e BULLYING → URGENTE, SAUDE e GRAVIDEZ → ALTA, o restante → NORMAL.
- Cada aluno pode ter apenas **um caso em andamento**.
- O status do caso segue o fluxo `ABERTO → EM_CONTATO → ENCAMINHADO → ACOMPANHANDO → RESOLVIDO`. De qualquer status em andamento, o caso pode ir para `ENCERRADO_SEM_RETORNO`. Caso encerrado não é reaberto.
- A situação do aluno só muda pelo endpoint de situação, usado pelo ms-casos. Aluno em busca ativa não pode ser excluído, e só casos encerrados podem ser excluídos.

## Arquitetura

```
              Swagger UI / Postman / Insomnia
                          │
                API Gateway :8082   (rotas lb://, sem URL fixa)
                 │                           │
   /api/v1/alunos/**                  /api/v1/casos/**
                 │                           │
                 ▼                           ▼
        ms-alunos (porta aleatória) ◄── ms-casos (porta aleatória)
        1..N instâncias          OpenFeign + LoadBalancer
                 │               + Circuit Breaker (Resilience4j) + Fallback
                 │                           │
        MySQL presente_alunos        MySQL presente_casos
        (container mysql-alunos      (container mysql-casos
         porta 3307)                  porta 3308)

             Eureka Server :8761 (gateway e microsserviços registrados)
```

| Componente | Porta | Função |
|---|---|---|
| eureka-server | 8761 | Service Discovery |
| api-gateway | 8082 | Ponto único de entrada, rotas com `lb://` via Eureka |
| ms-alunos | aleatória (`server.port=0`) | Alunos e situação escolar |
| ms-casos | aleatória (`server.port=0`) | Casos de busca ativa, cliente Feign do ms-alunos |
| mysql-alunos | 3307 | Banco `presente_alunos`, exclusivo do ms-alunos |
| mysql-casos | 3308 | Banco `presente_casos`, exclusivo do ms-casos |

## Tecnologias

- Java 25
- Spring Boot 4.0.9 e Spring Cloud 2025.1.4
- Spring Web MVC, Spring Data JPA, Bean Validation, Lombok
- Spring Cloud Netflix Eureka (Server e Client)
- Spring Cloud Gateway (Server WebFlux) e Spring Cloud LoadBalancer
- Spring Cloud OpenFeign
- Spring Cloud Circuit Breaker com Resilience4j
- MySQL 8.4 (Docker) e Flyway
- springdoc-openapi (Swagger UI)
- JUnit 5 e Mockito

## Estrutura do repositório

```
presente/
├── docker-compose.yml        MySQL de cada microsserviço
├── eureka-server/
├── api-gateway/
├── ms-alunos/
│   └── src/main/java/com/github/juanmarini/ms/alunos/
│       ├── controller/        AlunoController
│       ├── dto/               AlunoRequestDTO, AlunoSituacaoRequestDTO, AlunoResponseDTO
│       ├── entity/            Aluno, SituacaoAluno, IdiomaFamilia
│       ├── exceptions/        exceções customizadas, DTOs de erro e GlobalExceptionHandler
│       ├── repository/        AlunoRepository
│       └── service/           AlunoService
│   └── src/main/resources/db/migration/   V1 (tabela) e V2 (carga inicial)
└── ms-casos/
    └── src/main/java/com/github/juanmarini/ms/casos/
        ├── client/            AlunoClient (OpenFeign)
        ├── controller/        CasoBuscaAtivaController (Circuit Breaker e fallback)
        ├── dto/               CasoRequestDTO, CasoUpdateRequestDTO, CasoStatusRequestDTO, CasoResponseDTO, AlunoDTO
        ├── entity/            CasoBuscaAtiva, StatusCaso, MotivoFalta, RedeApoio, Prioridade
        ├── exceptions/        exceções customizadas, DTOs de erro e GlobalExceptionHandler
        ├── repository/        CasoBuscaAtivaRepository
        └── service/           CasoBuscaAtivaService
    └── src/main/resources/db/migration/   V1 (tabela) e V2 (carga inicial)
```

## Pré-requisitos

- **JDK 25** (`java -version` deve mostrar 25)
- **Docker Desktop** em execução (ou Docker Engine com Docker Compose)
- **Git**
- O Maven não precisa ser instalado: cada projeto tem o Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Opcional: IntelliJ IDEA

As portas **8761**, **8082**, **3307** e **3308** precisam estar livres.

## Como executar

### 1. Clonar o repositório

```bash
git clone https://github.com/juan-marini/presente.git
cd presente
```

### 2. Subir os bancos MySQL com Docker

```bash
docker compose up -d
docker compose ps
```

Aguarde os dois containers (`mysql-alunos` e `mysql-casos`) aparecerem como `healthy`, o que leva cerca de 30 segundos na primeira vez. Os bancos `presente_alunos` e `presente_casos` são criados automaticamente, e as tabelas e a carga inicial são criadas pelo **Flyway** quando cada microsserviço sobe.

| Banco | Host | Porta | Usuário | Senha |
|---|---|---|---|---|
| presente_alunos | localhost | 3307 | root | root |
| presente_casos | localhost | 3308 | root | root |

### 3. Subir os serviços, nesta ordem

Abra um terminal para cada serviço, a partir da raiz do repositório.

**Windows (CMD):**

```bat
cd eureka-server && mvnw.cmd spring-boot:run
cd api-gateway   && mvnw.cmd spring-boot:run
cd ms-alunos     && mvnw.cmd spring-boot:run
cd ms-casos      && mvnw.cmd spring-boot:run
```

**Windows (PowerShell):**

```powershell
cd eureka-server; .\mvnw.cmd spring-boot:run
cd api-gateway;   .\mvnw.cmd spring-boot:run
cd ms-alunos;     .\mvnw.cmd spring-boot:run
cd ms-casos;      .\mvnw.cmd spring-boot:run
```

**Linux, macOS ou Git Bash:**

```bash
cd eureka-server && ./mvnw spring-boot:run
cd api-gateway   && ./mvnw spring-boot:run
cd ms-alunos     && ./mvnw spring-boot:run
cd ms-casos      && ./mvnw spring-boot:run
```

1. **eureka-server**: aguarde o painel abrir em http://localhost:8761
2. **api-gateway**: porta 8082
3. **ms-alunos**: para testar o **Load Balancer**, rode este comando **duas vezes**, em dois terminais. Cada instância sobe em uma porta aleatória.
4. **ms-casos**

**Pelo IntelliJ IDEA:** abra cada pasta como projeto Maven (ou adicione as quatro como módulos Maven) e execute as classes `EurekaServerApplication`, `ApiGatewayApplication`, `MsAlunosApplication` e `MsCasosApplication`, nessa ordem, com o SDK 25. Para uma segunda instância do ms-alunos, use *Run Configuration → Modify options → Allow multiple instances*.

Os microsserviços usam o profile `dev` (MySQL), já ativado no `application.properties`. Não é preciso alterar nenhum arquivo.

No painel do Eureka (http://localhost:8761) devem aparecer `API-GATEWAY`, `MS-ALUNOS` (uma linha por instância) e `MS-CASOS`.

## Documentação OpenAPI / Swagger

Cada microsserviço publica o próprio Swagger UI. Como as portas são aleatórias, veja a porta da instância:

- no log de inicialização: `Tomcat started on port XXXXX`, ou
- no painel do Eureka, clicando no link da instância (troque `/actuator/info` por `/swagger-ui.html`).

Depois acesse:

- `http://localhost:{porta-do-ms-alunos}/swagger-ui.html`
- `http://localhost:{porta-do-ms-casos}/swagger-ui.html`

A especificação em JSON fica em `/v3/api-docs`. Todos os endpoints podem ser executados pelo Swagger UI, com exemplos de corpo.

## Endpoints (via API Gateway — http://localhost:8082)

### ms-alunos — `/api/v1/alunos`

| Método | Rota | Descrição | Retornos |
|---|---|---|---|
| GET | `/api/v1/alunos` | Lista alunos (filtro opcional `?situacao=EM_BUSCA_ATIVA`) | 200 |
| GET | `/api/v1/alunos/{id}` | Busca aluno por id | 200, 404 |
| POST | `/api/v1/alunos` | Cadastra aluno (situação inicial `REGULAR`) | 201 + `Location`, 400, 409 |
| PUT | `/api/v1/alunos/{id}` | Atualiza dados cadastrais | 200, 400, 404, 409 |
| DELETE | `/api/v1/alunos/{id}` | Exclui aluno que não esteja em busca ativa | 204, 404, 409 |
| PUT | `/api/v1/alunos/{id}/situacao` | Altera a situação (usado pelo ms-casos via OpenFeign) | 200, 400, 404, 409 |

### ms-casos — `/api/v1/casos`

| Método | Rota | Descrição | Retornos |
|---|---|---|---|
| GET | `/api/v1/casos` | Lista casos (filtros opcionais `?status=` e `?alunoId=`) | 200 |
| GET | `/api/v1/casos/{id}` | Busca caso por id com os dados do aluno (Feign) | 200, 404 |
| POST | `/api/v1/casos` | Abre caso e coloca o aluno `EM_BUSCA_ATIVA` (Feign) | 201 + `Location`, 400, 404, 409, 503 |
| PUT | `/api/v1/casos/{id}` | Atualiza motivo, resumo, encaminhamento, prioridade e agente | 200, 400, 404, 409 |
| DELETE | `/api/v1/casos/{id}` | Exclui caso encerrado | 204, 404, 409 |
| PATCH | `/api/v1/casos/{id}/status` | Altera o status do caso; `RESOLVIDO` e `ENCERRADO_SEM_RETORNO` atualizam o aluno (Feign) | 200, 400, 404, 409, 503 |

### Códigos de erro

| Situação | Exceção | HTTP |
|---|---|---|
| Campos inválidos (Bean Validation) | `MethodArgumentNotValidException` | 400 (lista de campos) |
| JSON malformado ou parâmetro com tipo errado | `HttpMessageNotReadableException` / `MethodArgumentTypeMismatchException` | 400 |
| Aluno ou caso inexistente | `ResourceNotFoundException` | 404 |
| Regra de negócio violada (matrícula duplicada, transição inválida, caso ativo existente) | exceções customizadas | 409 |
| ms-alunos indisponível ao abrir caso ou alterar status | `ServicoIndisponivelException` (fallback) | 503 |

## Roteiro de teste (via Gateway)

Os comandos `curl` abaixo são para Git Bash, Linux ou macOS. No Windows, rode-os no **Git Bash**, porque no PowerShell `curl` é um alias de `Invoke-WebRequest` e no CMD as aspas simples não funcionam. Todos os passos também podem ser feitos pelo Swagger UI ou pelo Postman.

A carga inicial do Flyway cria 10 alunos e 5 casos coerentes entre os dois bancos. Os alunos 2, 5 e 8 estão em busca ativa e têm caso em andamento, o aluno 3 retornou e o aluno 9 evadiu.

**1. Listar a carga inicial**

```bash
curl http://localhost:8082/api/v1/alunos
curl http://localhost:8082/api/v1/casos
```

**2. Consultar um caso com os dados do aluno (OpenFeign)**

```bash
curl http://localhost:8082/api/v1/casos/1
```

**3. Abrir um caso para um aluno `REGULAR` (aluno 1)**

```bash
curl -i -X POST http://localhost:8082/api/v1/casos \
  -H "Content-Type: application/json" \
  -d '{"alunoId":1,"motivo":"TRANSPORTE","resumo":"Familia informou que o aluno nao tem dinheiro para a conducao desde o inicio do mes.","agenteResponsavel":"Mariana Teixeira"}'
```

Retorna `201 Created` com o header `Location`, encaminhamento `PASSE_LIVRE` e prioridade `NORMAL` sugeridos, e o aluno já com situação `EM_BUSCA_ATIVA`. Para conferir:

```bash
curl http://localhost:8082/api/v1/alunos/1
```

**4. Avançar o caso até `RESOLVIDO` (troque `{id}` pelo id retornado no passo 3)**

```bash
curl -X PATCH http://localhost:8082/api/v1/casos/{id}/status -H "Content-Type: application/json" -d '{"status":"EM_CONTATO"}'
curl -X PATCH http://localhost:8082/api/v1/casos/{id}/status -H "Content-Type: application/json" -d '{"status":"ENCAMINHADO"}'
curl -X PATCH http://localhost:8082/api/v1/casos/{id}/status -H "Content-Type: application/json" -d '{"status":"ACOMPANHANDO"}'
curl -X PATCH http://localhost:8082/api/v1/casos/{id}/status -H "Content-Type: application/json" -d '{"status":"RESOLVIDO"}'
curl http://localhost:8082/api/v1/alunos/1
```

O aluno 1 passa para `RETORNOU`.

**5. Encerrar sem retorno (caso 4, aluno 8)**

```bash
curl -X PATCH http://localhost:8082/api/v1/casos/4/status -H "Content-Type: application/json" -d '{"status":"ENCERRADO_SEM_RETORNO"}'
curl http://localhost:8082/api/v1/alunos/8
```

O aluno 8 passa para `EVADIDO`.

**6. Cenários de erro**

```bash
# 404 - aluno inexistente
curl -i -X POST http://localhost:8082/api/v1/casos -H "Content-Type: application/json" \
  -d '{"alunoId":999,"motivo":"SAUDE","resumo":"Aluno com faltas frequentes por motivo de saude.","agenteResponsavel":"Mariana Teixeira"}'

# 409 - aluno 2 já possui caso em andamento
curl -i -X POST http://localhost:8082/api/v1/casos -H "Content-Type: application/json" \
  -d '{"alunoId":2,"motivo":"SAUDE","resumo":"Aluno com faltas frequentes por motivo de saude.","agenteResponsavel":"Mariana Teixeira"}'

# 409 - transição de status inválida (caso 1 está EM_CONTATO)
curl -i -X PATCH http://localhost:8082/api/v1/casos/1/status -H "Content-Type: application/json" -d '{"status":"RESOLVIDO"}'

# 400 - dados inválidos
curl -i -X POST http://localhost:8082/api/v1/alunos -H "Content-Type: application/json" \
  -d '{"matricula":"","nome":"Jo","telefoneResponsavel":"123","faltasNoBimestre":-1}'
```

**7. Load Balancer**

Com duas instâncias do ms-alunos em execução, repita algumas vezes:

```bash
curl http://localhost:8082/api/v1/casos/1
```

Nos logs das duas instâncias do ms-alunos aparece, alternadamente, a linha `ms-alunos atendendo GET /api/v1/alunos/1 na porta XXXXX`, com portas diferentes.

**8. Circuit Breaker e Fallback**

Pare todas as instâncias do ms-alunos e chame:

```bash
curl http://localhost:8082/api/v1/casos/1
```

O caso é retornado sem os dados do aluno, com `"mensagem": "Dados do aluno temporariamente indisponíveis"` (fallback). Depois de algumas falhas, o circuito abre e o fallback responde direto, sem tentar chamar o ms-alunos.

```bash
curl -i -X POST http://localhost:8082/api/v1/casos -H "Content-Type: application/json" \
  -d '{"alunoId":4,"motivo":"SAUDE","resumo":"Aluno com faltas frequentes por motivo de saude.","agenteResponsavel":"Mariana Teixeira"}'
```

Retorna `503 Service Unavailable` e o caso **não** é gravado: a transação é desfeita, mantendo os dois bancos consistentes. Ao subir o ms-alunos novamente, o circuito volta a fechar depois do tempo de espera (50 s).

Pare as instâncias com **Ctrl+C** no terminal (ou com o botão *Exit* do IntelliJ), para que elas saiam do registro do Eureka. Se uma instância for encerrada à força, ela pode continuar aparecendo no painel do Eureka; nesse caso, reinicie o eureka-server e aguarde os serviços se registrarem novamente.

## Testes de unidade

```bash
cd ms-alunos && ./mvnw test
cd ms-casos  && ./mvnw test
```

## Encerrando o ambiente

```bash
docker compose down        # para os containers e mantém os dados
docker compose down -v     # para os containers e apaga os dados (a carga do Flyway é refeita na próxima execução)
```
