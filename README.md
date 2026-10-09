# MOTIVA — Sprint 4

**Sistema de Monitoramento e Priorização de Roçada de Vegetação em Rodovias**

API REST em Spring Boot + Spring Data JPA, migrada a partir do protótipo de console com JDBC puro da Sprint 3.

> **Este repositório reúne todas as sprints do Challenge.** O código das Sprints 1 a 3 continua versionado aqui, intocado, em `src/dao`, `src/db`, `src/model`, `src/service` e `src/main/Main.java`. A documentação daquelas sprints — incluindo as perguntas de reflexão de cada uma — está em **[`docs/README-sprints-1-3.md`](docs/README-sprints-1-3.md)**.
>
> O projeto Spring Boot da Sprint 4 vive em `src/main/java/br/com/motiva`. Como o Maven só compila `src/main/java`, as duas versões convivem no mesmo repositório sem conflito: o build da Sprint 4 ignora o código de console das sprints anteriores.

---

## Índice

1. [O que mudou da Sprint 3 para a Sprint 4](#1-o-que-mudou-da-sprint-3-para-a-sprint-4)
2. [Arquitetura em camadas](#2-arquitetura-em-camadas)
3. [Modelo de domínio](#3-modelo-de-domínio)
4. [Como executar](#4-como-executar)
5. [Tabela de endpoints](#5-tabela-de-endpoints)
6. [Exemplos cURL](#6-exemplos-curl)
7. [Regras de negócio](#7-regras-de-negócio)
8. [Derived queries](#8-derived-queries)
9. [Testes](#9-testes)
10. [Perguntas de reflexão](#10-perguntas-de-reflexão)
11. [Boas práticas de Git adotadas](#11-boas-práticas-de-git-adotadas)

---

## 1. O que mudou da Sprint 3 para a Sprint 4

O domínio é o mesmo. O que mudou foi a forma de conversar com o banco e com o mundo externo.

| Sprint 3 (JDBC puro) | Sprint 4 (Spring Boot) | Papel |
|---|---|---|
| `model/` + `record` interno do DAO | `@Entity` em `model/` | Classe mapeada para a tabela via JPA |
| `EquipeManutencaoDAO` (136 linhas) | `EquipeManutencaoRepository` (interface, 0 linhas de corpo) | Acesso a dados sem SQL manual |
| `db.ConexaoBD` (Singleton) | `application.properties` + pool HikariCP | Conexão gerenciada pelo framework |
| `service.GeradorRelatorio` | `RelatorioPrioridadeService` (`@Service`) | Regras de negócio — continua existindo |
| `Main.main()` com `System.out.println` | `@RestController` + JSON | Interface do sistema vira HTTP |
| Teste manual no console | cURL / Postman + `@SpringBootTest` | A API é consumida por qualquer cliente |

O exemplo mais direto da diferença, gravar um trecho no banco:

```java
// Sprint 3 — TrechoRodoviaDAO.inserir(), resumido
Connection conn = ConexaoBD.getInstancia().conectar();
try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERIR, new String[]{"ID"})) {
    preencherParametrosComuns(stmt, trecho);   // 8 setters, um por coluna
    stmt.executeUpdate();
    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) trecho.setId(generatedKeys.getLong(1));
    }
}

// Sprint 4 — TrechoRodoviaService.criar()
trechoRepository.save(trecho);
```

### O que NÃO foi usado nesta sprint

Conforme o item 4 do enunciado, o projeto não contém:

- `Connection`, `PreparedStatement` ou `ResultSet` em lugar nenhum;
- SQL manual dentro de `@Service` ou `@RestController` — não há `@Query` no projeto, todas as consultas são *derived queries*;
- lógica de negócio dentro de `@RestController`;
- `System.out.println` como interface do sistema — a saída é JSON.

> Para conferir, este comando não retorna nenhuma linha — as únicas ocorrências desses termos no projeto estão em comentários que explicam a migração, e o segundo `grep` as descarta:
>
> ```bash
> grep -rn "PreparedStatement\|ResultSet\|System.out.print\|java.sql" src/main/java | grep -v '\*'
> ```

---

## 2. Arquitetura em camadas

```
src/main/java/br/com/motiva/
├── MotivaApplication.java      # @SpringBootApplication (main)
├── model/                      # @Entity — entidades da Sprint 3
│   ├── EquipeManutencao.java
│   ├── TrechoRodovia.java            (abstrata, @Inheritance SINGLE_TABLE)
│   ├── TrechoUmido.java              (@DiscriminatorValue "UMIDO")
│   ├── TrechoSeco.java               (@DiscriminatorValue "SECO")
│   ├── TrechoUmidoMonitorado.java    (@DiscriminatorValue "UMIDO_MONITORADO")
│   ├── MonitoravelViaIoT.java        (interface — Sprint 2)
│   ├── IntervencaoOperacional.java   (abstrata, @Inheritance SINGLE_TABLE)
│   ├── RocadaMecanizada.java         (@DiscriminatorValue "ROCADA_MECANIZADA")
│   ├── Pulverizacao.java             (@DiscriminatorValue "PULVERIZACAO")
│   ├── RelatorioPrioridade.java
│   ├── Prioridade.java               (enum com os limiares da Sprint 2)
│   └── converter/BooleanSNConverter.java
├── repository/                 # Interfaces JpaRepository — SEM SQL
├── service/                    # @Service — regras de negócio
├── controller/                 # @RestController — endpoints REST
├── dto/                        # Contratos de entrada e saída da API
└── exception/                  # @RestControllerAdvice + exceções de domínio
```

**Fluxo entre camadas:** `Controller → Service → Repository → Banco`. Cada camada só conversa com a vizinha. O Controller nunca enxerga um `Repository`, e o `Repository` nunca enxerga um DTO.

---

## 3. Modelo de domínio

As quatro tabelas são **as mesmas da Sprint 3**. A única alteração de schema foi na geração de ID, explicada em [Como executar](#4-como-executar).

```
EQUIPE_MANUTENCAO 1 ──────< TRECHO_RODOVIA
        │                        │
        │                        │
        └────────< INTERVENCAO_OPERACIONAL >────┘

RELATORIO_PRIORIDADE   (snapshot independente, sem FK)
```

### Herança mapeada em tabela única

As hierarquias `TrechoRodovia` e `IntervencaoOperacional` usam `SINGLE_TABLE`: toda a hierarquia vive em uma tabela só, separada por uma coluna discriminadora (`TIPO_TRECHO` e `TIPO_INTERVENCAO`). É exatamente a mesma modelagem que a Sprint 3 já tinha — a diferença é que agora quem lê o discriminador e instancia a subclasse certa é o Hibernate, e não um `switch` escrito à mão dentro do DAO.

| Classe | Discriminador | Taxa de crescimento |
|---|---|---|
| `TrechoUmido` | `UMIDO` | 3,5 cm/dia × índice pluviométrico |
| `TrechoSeco` | `SECO` | 1,2 cm/dia (×0,6 em estação seca) |
| `TrechoUmidoMonitorado` | `UMIDO_MONITORADO` | herda de `TrechoUmido` + sensor IoT |

---

## 4. Como executar

### Pré-requisitos

- **Java 17** (o projeto não compila em versões anteriores)
- **Maven** — opcional: o repositório traz o Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Acesso ao **Oracle da FIAP** com as tabelas da Sprint 3 criadas

### Passo 1 — Migrar o banco da Sprint 3

A Sprint 3 gerava o ID com `GENERATED ALWAYS AS IDENTITY`. O item 5.1 do enunciado desta sprint pede `@GeneratedValue` com `@SequenceGenerator` ligado a *sequences*, e o Oracle recusa qualquer `INSERT` que informe o ID de uma coluna `GENERATED ALWAYS`. Por isso a identity precisa sair e dar lugar a uma sequence.

Conecte no Oracle com o mesmo usuário da Sprint 3 e rode:

```sql
@sql/02_migracao_sprint3_para_sprint4.sql
```

O script é idempotente e **preserva todos os dados**: ele remove a identity de cada tabela e cria a sequence correspondente começando em `MAX(ID) + 1`, para não colidir com as linhas já gravadas.

Se preferir montar o schema do zero, use no lugar dele:

```sql
@sql/01_create_tables_sprint4.sql
@sql/03_insert_data_sprint4.sql
```

| Arquivo | Quando usar |
|---|---|
| `sql/01_create_tables_sprint4.sql` | Schema vazio — cria tabelas e sequences do zero |
| `sql/02_migracao_sprint3_para_sprint4.sql` | **Caso comum** — você já rodou a Sprint 3 e quer manter os dados |
| `sql/03_insert_data_sprint4.sql` | Dados de teste, só depois do script 01 |

### Passo 2 — Definir as credenciais

Nenhuma senha fica no código nem no `application.properties`: o projeto lê as variáveis de ambiente `ORACLE_USER` e `ORACLE_PASSWORD`.

**Windows (PowerShell):**

```powershell
$env:ORACLE_USER = "rm564878"
$env:ORACLE_PASSWORD = "sua_senha"
```

**Linux / macOS:**

```bash
export ORACLE_USER=rm564878
export ORACLE_PASSWORD=sua_senha
```

**IntelliJ IDEA:** `Run > Edit Configurations > Environment variables` →
`ORACLE_USER=rm564878;ORACLE_PASSWORD=sua_senha`

Se o seu laboratório usar `SERVICE_NAME` em vez de `SID`, defina também:

```bash
export ORACLE_URL='jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL'
```

### Passo 3 — Subir a aplicação

```bash
./mvnw spring-boot:run
```

No Windows, `mvnw.cmd spring-boot:run`. Se você já tem o Maven instalado, `mvn spring-boot:run` também funciona.

A API sobe em <http://localhost:8080>.

### Modo demonstração (sem Oracle)

Para demonstrar a API em uma máquina sem o banco configurado — ou quando o Oracle da FIAP está fora do ar — existe o perfil `demo`, que roda com um H2 em memória:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Nesse modo o Hibernate cria o schema a partir das próprias entidades, inclusive as quatro sequences. Os dados somem quando a aplicação para. **O banco oficial do projeto continua sendo o Oracle da FIAP.**

---

## 5. Tabela de endpoints

### Trechos — `/api/trechos`

| Método | Rota | Ação | Respostas |
|---|---|---|---|
| `GET` | `/api/trechos` | Listar todos | `200 OK` |
| `GET` | `/api/trechos?tipo=SECO` | Filtrar por tipo (*derived query*) | `200 OK` |
| `GET` | `/api/trechos?nivelMinimoCm=50` | Filtrar por nível mínimo (*derived query*) | `200 OK` |
| `GET` | `/api/trechos?semEquipe=true` | Trechos sem equipe designada | `200 OK` |
| `GET` | `/api/trechos/{id}` | Buscar por ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/trechos` | Criar | `201 Created` + `Location` / `400 Bad Request` |
| `PUT` | `/api/trechos/{id}` | Atualizar | `200 OK` / `404` / `400` |
| `POST` | `/api/trechos/{id}/simulacao?dias=10` | Simular crescimento (motor da Sprint 2) | `200 OK` / `404` / `400` |
| `DELETE` | `/api/trechos/{id}` | Remover | `204 No Content` / `404` / `400` |

### Equipes — `/api/equipes`

| Método | Rota | Ação | Respostas |
|---|---|---|---|
| `GET` | `/api/equipes` | Listar todas | `200 OK` |
| `GET` | `/api/equipes?nome=alpha` | Buscar por parte do nome | `200 OK` |
| `GET` | `/api/equipes?efetivoMinimo=5` | Equipes com efetivo mínimo | `200 OK` |
| `GET` | `/api/equipes/{id}` | Buscar por ID | `200 OK` / `404` |
| `POST` | `/api/equipes` | Criar | `201 Created` + `Location` / `400` |
| `PUT` | `/api/equipes/{id}` | Atualizar | `200 OK` / `404` / `400` |
| `DELETE` | `/api/equipes/{id}` | Remover | `204 No Content` / `404` / `400` |

### Intervenções — `/api/intervencoes`

| Método | Rota | Ação | Respostas |
|---|---|---|---|
| `GET` | `/api/intervencoes` | Listar todas | `200 OK` |
| `GET` | `/api/intervencoes?tipo=PULVERIZACAO` | Filtrar por tipo (*derived query*) | `200 OK` |
| `GET` | `/api/intervencoes?trechoId=3` | Histórico de um trecho | `200 OK` / `404` |
| `GET` | `/api/intervencoes/{id}` | Buscar por ID | `200 OK` / `404` |
| `POST` | `/api/intervencoes` | Registrar e executar | `201 Created` + `Location` / `404` / `400` |
| `PUT` | `/api/intervencoes/{id}` | Atualizar | `200 OK` / `404` / `400` |
| `DELETE` | `/api/intervencoes/{id}` | Remover | `204 No Content` / `404` |

### Relatórios — `/api/relatorios`

| Método | Rota | Ação | Respostas |
|---|---|---|---|
| `POST` | `/api/relatorios` | Gerar o relatório e gravar no histórico | `201 Created` + `Location` / `400` |
| `GET` | `/api/relatorios` | Listar o histórico | `200 OK` |
| `GET` | `/api/relatorios/periodo?inicio=...&fim=...` | Consultar por período | `200 OK` / `400` |
| `GET` | `/api/relatorios/{id}` | Buscar um snapshot por ID | `200 OK` / `404` |

### Códigos HTTP devolvidos pela API

| Código | Quando |
|---|---|
| `200 OK` | Consulta ou atualização bem-sucedida |
| `201 Created` | Recurso criado — corpo do recurso + cabeçalho `Location` |
| `204 No Content` | Remoção bem-sucedida, sem corpo |
| `400 Bad Request` | Falha de validação (`@Valid`) ou violação de regra de negócio |
| `404 Not Found` | ID inexistente |
| `409 Conflict` | Violação de integridade no banco |
| `500 Internal Server Error` | Erro inesperado — detalhes só no log do servidor |

Todas as respostas de erro usam o mesmo formato:

```json
{
  "timestamp": "2026-10-09T11:01:07.4387056",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Um ou mais campos da requisicao sao invalidos.",
  "caminho": "/api/trechos",
  "campos": {
    "nivelVegetacaoCm": ["O nivel de vegetacao nao pode ser negativo."]
  }
}
```

---

## 6. Exemplos cURL

> O arquivo [`docs/evidencias-requisicoes.md`](docs/evidencias-requisicoes.md) traz **23 requisições reais**, com o comando enviado, o código HTTP e o corpo devolvido. Ele foi gerado pelo script [`docs/exemplos-curl.sh`](docs/exemplos-curl.sh), que você pode rodar de novo a qualquer momento:
>
> ```bash
> bash docs/exemplos-curl.sh > docs/evidencias-requisicoes.md
> ```

### Criar uma equipe

```bash
curl -i -X POST http://localhost:8080/api/equipes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Equipe Alpha","quantidadeIntegrantes":6}'
```

```
HTTP/1.1 201 Created
Location: http://localhost:8080/api/equipes/1

{"id":1,"nome":"Equipe Alpha","quantidadeIntegrantes":6}
```

### Criar um trecho de cada tipo

```bash
# Trecho seco em estação seca, vegetação urgente
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"SECO","quilometroInicial":30,"quilometroFinal":40,
       "nivelVegetacaoCm":88.0,"emEstacaoSeca":true}'

# Trecho úmido com chuva 80% acima do normal
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"UMIDO","quilometroInicial":10,"quilometroFinal":20,
       "nivelVegetacaoCm":56.0,"indicePluviometrico":1.8}'

# Trecho úmido com sensor IoT
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"UMIDO_MONITORADO","quilometroInicial":0,"quilometroFinal":10,
       "nivelVegetacaoCm":30.0,"indicePluviometrico":1.2,
       "idSensor":"SENSOR-BR116-KM05"}'
```

A resposta mostra o polimorfismo da Sprint 2 em campos calculados na hora, que não existem em coluna nenhuma:

```json
{
  "id": 1,
  "tipo": "SECO",
  "descricaoTipo": "Trecho Seco",
  "quilometroInicial": 30,
  "quilometroFinal": 40,
  "nivelVegetacaoCm": 88.0,
  "taxaCrescimentoDiarioCm": 0.72,
  "critico": true,
  "prioridade": "URGENTE",
  "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
  "emEstacaoSeca": true
}
```

### Filtrar com as derived queries

```bash
curl -i 'http://localhost:8080/api/trechos?tipo=SECO'
curl -i 'http://localhost:8080/api/trechos?nivelMinimoCm=50'
```

### Simular o crescimento da vegetação

```bash
curl -i -X POST 'http://localhost:8080/api/trechos/3/simulacao?dias=10'
```

### Registrar uma intervenção

```bash
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"ROCADA_MECANIZADA","trechoAlvoId":1,"equipeResponsavelId":1}'
```

```json
{
  "id": 1,
  "tipo": "ROCADA_MECANIZADA",
  "descricaoTipo": "Roçada Mecanizada",
  "dataExecucao": "2026-10-09T11:01:06",
  "trechoAlvo": { "id": 1, "nivelVegetacaoCm": 20.0, "...": "..." },
  "equipeResponsavel": { "id": 1, "nome": "Equipe Alpha", "...": "..." },
  "resultadoExecucao": "Roçada mecanizada concluída com trator e roçadeira lateral. Nível antes: 88,0 cm; nível após: 20,0 cm (residual de segurança)."
}
```

O campo `resultadoExecucao` é o retorno de `executarServico()` — o texto que, nas Sprints 2 e 3, ia para o console. Repare que a roçada **derrubou o nível do trecho para 20 cm**: o efeito polimórfico foi gravado no banco pelo *dirty checking* do Hibernate, sem nenhum `UPDATE` escrito à mão.

### Gerar e consultar o relatório de prioridade

```bash
curl -i -X POST http://localhost:8080/api/relatorios

curl -i http://localhost:8080/api/relatorios

curl -i 'http://localhost:8080/api/relatorios/periodo?inicio=2026-01-01T00:00:00&fim=2026-12-31T23:59:59'
```

```json
{
  "relatorio": {
    "id": 1,
    "dataGeracao": "2026-10-09T11:01:06",
    "qtUrgente": 0, "qtCritico": 1, "qtAtencao": 2, "qtNormal": 1,
    "totalTrechosAnalisados": 4,
    "resumo": "4 trecho(s) analisado(s): 0 urgente(s), 1 critico(s), 2 em atencao, 1 normal(is)."
  },
  "itens": [
    {
      "trechoId": 4,
      "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
      "nivelVegetacaoCm": 30.68,
      "leituraSensorCm": 30.68,
      "prioridade": "ATENCAO",
      "intervencaoRecomendada": "Agendar roçada manual nas próximas 2 semanas"
    }
  ]
}
```

### Erros

```bash
# 404 — trecho inexistente
curl -i http://localhost:8080/api/trechos/999999

# 400 — Bean Validation barra o nível negativo
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"SECO","quilometroInicial":90,"quilometroFinal":100,"nivelVegetacaoCm":-5.0}'

# 400 — regra de negócio do Service barra a quilometragem invertida
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"SECO","quilometroInicial":100,"quilometroFinal":95,"nivelVegetacaoCm":10.0}'
```

---

## 7. Regras de negócio

Todas vivem na camada `@Service`. Nenhuma está no Controller.

### Motor de prioridade (`RelatorioPrioridadeService`)

Os limiares são os mesmos da Sprint 2, agora no enum `Prioridade`:

| Nível de vegetação | Prioridade | Intervenção recomendada |
|---|---|---|
| ≥ 80 cm | `URGENTE` | Roçada Mecanizada — despachar equipe imediatamente |
| ≥ 50 cm | `CRITICO` | Pulverização herbicida + reavaliar em 7 dias |
| ≥ 25 cm | `ATENCAO` | Agendar roçada manual nas próximas 2 semanas |
| < 25 cm | `NORMAL` | Monitoramento de rotina |

Antes de classificar, o serviço consulta os sensores de todos os trechos que implementam `MonitoravelViaIoT` — o teste é feito **contra a interface**, não contra uma classe concreta, de modo que um futuro `TrechoUrbanoMonitorado` entraria na rotina sem alterar uma linha do serviço.

### Validações (`TrechoRodoviaService`)

| Regra | Onde | Resposta |
|---|---|---|
| `nivelVegetacaoCm >= 0` | `@PositiveOrZero` no DTO | `400` |
| `quilometroInicial >= 0` | `@PositiveOrZero` no DTO | `400` |
| `quilometroFinal > quilometroInicial` | Service (compara 2 campos) | `400` |
| `indicePluviometrico >= 1.0` | `@DecimalMin` no DTO | `400` |
| `UMIDO_MONITORADO` exige `idSensor` | Service (depende do tipo) | `400` |
| Não existir outro trecho na mesma faixa de km | Service (consulta o banco) | `400` |
| Tipo do trecho não pode mudar em um `PUT` | Service | `400` |
| Trecho com intervenções não pode ser removido | Service (consulta o banco) | `400` |

### Validações (`EquipeManutencaoService`)

| Regra | Onde | Resposta |
|---|---|---|
| Nome não vazio, até 100 caracteres | `@NotBlank` / `@Size` no DTO | `400` |
| `quantidadeIntegrantes >= 1` | `@Positive` no DTO | `400` |
| Nome de equipe não pode repetir | Service (consulta o banco) | `400` |
| Equipe com trechos ou histórico não pode ser removida | Service (consulta o banco) | `400` |

### Validações (`IntervencaoOperacionalService`)

| Regra | Resposta |
|---|---|
| `PULVERIZACAO` exige `tipoProduto` | `400` |
| `ROCADA_MECANIZADA` não aceita `tipoProduto` | `400` |
| Não se despacha equipe para trecho classificado como `NORMAL` (< 25 cm) | `400` |
| Trecho ou equipe inexistentes | `404` |

A última regra é nossa: o objetivo do MOTIVA é priorizar roçada onde ela é necessária, então autorizar intervenção em vegetação baixa desperdiçaria equipe e contradiria o próprio relatório de prioridade.

### A divisão entre DTO e Service

A regra que usamos para decidir onde cada validação mora:

- **Bean Validation no DTO** quando dá para decidir olhando **um campo isolado** (`nivelVegetacaoCm >= 0`);
- **Service** quando é preciso **comparar dois campos** (`kmFinal > kmInicial`), **consultar o banco** (nome duplicado) ou **saber o tipo** do objeto (`idSensor` obrigatório só para monitorado).

---

## 8. Derived queries

Nenhum `@Query` e nenhuma linha de SQL foram escritos. Todas as consultas nascem do **nome do método**:

```java
// TrechoRodoviaRepository — as duas exigidas no item 3.4 do enunciado
List<TrechoRodovia> findByNivelVegetacaoCmGreaterThanEqual(Double minimo);
List<TrechoRodovia> findByTipo(String tipo);

// Navegando pela associação @ManyToOne
List<TrechoRodovia> findByEquipeResponsavelId(Long idEquipe);
List<TrechoRodovia> findByEquipeResponsavelIsNull();

// RelatorioPrioridadeRepository — sustenta o endpoint /periodo
List<RelatorioPrioridade> findByDataGeracaoBetweenOrderByDataGeracaoDesc(
        LocalDateTime inicio, LocalDateTime fim);

// IntervencaoOperacionalRepository
List<IntervencaoOperacional> findByTrechoAlvoIdOrderByDataExecucaoDesc(Long idTrecho);
```

`findByTipo` merece uma nota. `TIPO_TRECHO` é a coluna discriminadora da herança, e o JPA não a expõe como atributo. Para que a *derived query* do enunciado funcionasse, declaramos um espelho somente-leitura dela em `TrechoRodovia`:

```java
@Column(name = "TIPO_TRECHO", insertable = false, updatable = false)
private String tipo;
```

O `insertable = false, updatable = false` é essencial: a coluna já é escrita pelo mecanismo de herança, e sem isso o Hibernate acusaria dois mapeamentos disputando a mesma coluna.

Para **ver** o SQL gerado, a aplicação sobe com `spring.jpa.show-sql=true`. Chamar `GET /api/trechos?tipo=SECO` imprime no console algo como:

```sql
select t1_0.ID, t1_0.TIPO_TRECHO, t1_0.EM_ESTACAO_SECA, t1_0.ID_EQUIPE_RESPONSAVEL,
       t1_0.ID_SENSOR, t1_0.INDICE_PLUVIOMETRICO, t1_0.NIVEL_VEGETACAO_CM,
       t1_0.QUILOMETRO_FINAL, t1_0.QUILOMETRO_INICIAL
  from TRECHO_RODOVIA t1_0
 where t1_0.TIPO_TRECHO = ?
```

---

## 9. Testes

```bash
./mvnw test
```

```
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

A classe `MotivaApiIntegrationTest` usa `@SpringBootTest` + `MockMvc` (item de bônus do enunciado) e sobe o contexto Spring inteiro contra um H2 em memória. Além de testar as rotas, isso **valida o mapeamento JPA**: se um `@Column` apontasse para uma coluna inexistente ou um `@SequenceGenerator` estivesse mal declarado, o contexto nem subiria.

O que a suíte cobre:

- CRUD completo de equipe e de trecho, conferindo `201 → 200 → 200 → 204 → 404`;
- cabeçalho `Location` nas respostas `201`;
- polimorfismo da taxa de crescimento das três subclasses (1,2 / 0,72 / 6,3 cm/dia);
- campos exclusivos de subtipo saindo e sumindo do JSON conforme o tipo;
- as duas *derived queries* do item 3.4;
- simulação de crescimento;
- efeito polimórfico de `executarServico()` — a roçada derruba o trecho para 20 cm, a pulverização não mexe no nível;
- as regras de negócio que devolvem `400`;
- geração do relatório, persistência do histórico e consulta por período.

Cada teste é `@Transactional`, então tudo o que ele grava é desfeito ao final.

---

## 10. Perguntas de reflexão

### 1. Por que o Repository é uma interface e não uma classe? Quem escreve a implementação e quando?

Porque uma interface descreve **o que** queremos do banco, e isso é tudo o que precisamos escrever. O **como** — abrir conexão, montar o SQL, posicionar parâmetros, percorrer o `ResultSet`, mapear colunas para atributos, fechar tudo — é mecânico e idêntico para qualquer entidade. Era justamente esse código repetitivo que ocupava a maior parte dos nossos DAOs na Sprint 3: os quatro DAOs daquele projeto somavam 633 linhas e eram variações do mesmo esqueleto, com os nomes das tabelas trocados.

Quem escreve a implementação é o **Spring Data JPA**, e ele a escreve **em tempo de execução**, quando a aplicação sobe. Ao encontrar uma interface que estende `JpaRepository`, o Spring gera dinamicamente um objeto que a implementa (um *proxy*) e o registra como bean. Esse proxy delega as operações padrão para o `SimpleJpaRepository` e, para os métodos que nós declaramos, usa o nome do método para montar a consulta.

Não existe `TrechoRodoviaRepositoryImpl` em lugar nenhum deste projeto — e, ainda assim, `trechoRepository.save(trecho)` funciona. É possível comprovar isso imprimindo a classe real do bean injetado: ela não é uma classe nossa, é algo como `jdk.proxy.$Proxy123`.

A vantagem prática é que o código que a gente escreve passou a ser só o que é específico do MOTIVA. O `EquipeManutencaoRepository` tem quatro assinaturas de método e nenhum corpo, no lugar das 136 linhas do `EquipeManutencaoDAO`.

### 2. O pattern DAO da Sprint 3 "morreu" na migração ou só mudou de forma? Explique com as suas palavras.

Só mudou de forma — e, na verdade, nem isso: o padrão continua exatamente onde estava, só que agora quem o implementa não somos nós.

O DAO é um padrão de **projeto**, não uma tecnologia. Ele diz: isole o acesso a dados atrás de uma interface, para que o resto do sistema peça objetos de domínio sem saber se eles vêm de Oracle, de um arquivo ou de memória. Essa ideia continua intacta na Sprint 4. O `RelatorioPrioridadeService` pede `trechoRepository.findAll()` e recebe trechos prontos, sem a menor noção de onde eles estavam — exatamente como o `GeradorRelatorio` da Sprint 3 pedia `trechoDAO.listarTodas()`.

O que morreu foi a **implementação manual** do padrão. Comparando papel a papel:

| Papel do DAO | Sprint 3 | Sprint 4 |
|---|---|---|
| Contrato de acesso a dados | métodos públicos do `TrechoRodoviaDAO` | interface `TrechoRodoviaRepository` |
| Tradução objeto ↔ linha | `mapearLinha()` e `paraDominio()` escritos por nós | anotações `@Entity` / `@Column` lidas pelo Hibernate |
| Reconstrução da subclasse certa | `switch` sobre `TIPO_TRECHO` dentro do DAO | `@DiscriminatorValue` resolvido pelo Hibernate |
| Gerenciamento de conexão | `ConexaoBD` (Singleton) | pool do Spring, configurado em `application.properties` |

A frase do enunciado que resume isso é precisa: **o Repository do Spring Data é um DAO gerado pelo framework em tempo de execução**. Trocamos trabalho manual e repetitivo por configuração declarativa, sem abrir mão da separação de responsabilidades que o padrão garante.

### 3. Por que a validação de `nivelVegetacao >= 0` (Sprint 1) deve ficar no Service e não no Controller?

Porque ela é uma regra do **MOTIVA**, e não um detalhe do **HTTP**.

O Controller é só uma das portas de entrada possíveis do sistema. Hoje é a única, mas o próprio histórico deste projeto mostra o contrário: na Sprint 3 a porta era um menu de console. Se amanhã o sistema ganhar um importador de CSV da concessionária, um job agendado que lê sensores ou uma fila de mensagens, todos eles vão chamar o `TrechoRodoviaService` — e todos vão herdar a validação automaticamente. Se ela estivesse no Controller, valeria apenas para quem entrasse pela porta do HTTP, e cada nova porta teria que reimplementá-la. Validação duplicada é validação que uma hora vai divergir.

Há outras duas razões concretas:

- **Testabilidade.** Dá para testar a regra chamando o Service diretamente, sem subir servidor nem simular requisição.
- **Vocabulário.** O Service fala a língua do negócio — ele lança `RegraDeNegocioException("O quilometro final deve ser maior que o inicial")`. Quem traduz isso para `400 Bad Request` é o `@RestControllerAdvice`. Assim a regra não precisa conhecer códigos HTTP, e o HTTP não precisa conhecer a regra.

Uma observação sobre como aplicamos isso na prática: o `nivelVegetacao >= 0` em si está declarado como `@PositiveOrZero` no DTO, porque é uma checagem de **formato** sobre um campo isolado, e o Bean Validation já devolve a mensagem por campo de graça. O que exige conhecimento do domínio ficou no Service: `quilometroFinal > quilometroInicial` (compara dois campos), `idSensor` obrigatório só para trechos monitorados (depende do tipo) e a recusa de remover um trecho com histórico (consulta o banco). O princípio é o mesmo nos dois casos — **nada disso está no Controller**.

### 4. No JDBC puro vocês escreviam SQL. Onde está o SQL do `findByTipo()`? Quem o gerou?

O SQL **não existe** enquanto a aplicação está parada. Ele não está no nosso código, não está em arquivo de configuração e não está em nenhum `@Query`. Ele passa a existir quando a aplicação sobe, e quem o escreve é o **Spring Data JPA junto com o Hibernate**.

O processo tem três etapas:

1. **Na subida da aplicação**, o Spring Data encontra `findByTipo` em `TrechoRodoviaRepository`. Ele remove o prefixo `findBy` e quebra o resto em partes: `Tipo`. Procura então um atributo chamado `tipo` na entidade `TrechoRodovia` — que é o nosso espelho da coluna discriminadora. Como não há sufixo de operador (`GreaterThanEqual`, `Containing`, `IsNull`…), assume igualdade.

2. Com isso ele monta uma consulta **JPQL**, que fala de objetos e não de tabelas: algo equivalente a `select t from TrechoRodovia t where t.tipo = :tipo`. Nesse ponto ainda não há SQL.

3. **Na primeira execução**, o Hibernate traduz essa JPQL para o SQL do banco configurado, usando o dialeto (`OracleDialect`) e o mapeamento das anotações para saber que `TrechoRodovia` é a tabela `TRECHO_RODOVIA` e que `tipo` é a coluna `TIPO_TRECHO`.

Dá para ver o resultado: a aplicação sobe com `spring.jpa.show-sql=true`, e o console imprime o SQL de cada consulta. Chamando `GET /api/trechos?tipo=SECO`, aparece o `select ... from TRECHO_RODOVIA t1_0 where t1_0.TIPO_TRECHO = ?` mostrado na [seção 8](#8-derived-queries).

Vale notar o que ganhamos nessa troca. O `?` daquele SQL é um parâmetro vinculado, não concatenação de texto — ou seja, consultas geradas assim são imunes a SQL injection por construção, enquanto no JDBC puro isso dependia da nossa disciplina de sempre usar `PreparedStatement`. E, porque a tradução para SQL acontece pelo dialeto, a mesma `findByTipo` roda contra o Oracle da FIAP em produção e contra o H2 em memória na suíte de testes, sem uma linha de diferença no código.

---

## 11. Boas práticas de Git adotadas

- **Commits incrementais e temáticos**, seguindo *Conventional Commits* (`feat:`, `docs:`, `test:`, `chore:`), um por etapa da migração — e não um único commit na véspera da entrega.
- **`.gitignore` adequado**: `target/`, arquivos de IDE (`.idea/`, `*.iml`, `.vscode/`, `.settings`), arquivos de sistema (`.DS_Store`, `Thumbs.db`) e logs.
- **Nenhuma credencial no repositório.** Usuário e senha do Oracle vêm das variáveis de ambiente `ORACLE_USER` e `ORACLE_PASSWORD`; o `application.properties` só referencia os nomes delas. O `.gitignore` também bloqueia `.env` e `application-local.properties`, caso alguém crie um arquivo local com valores reais.
- **Driver via Maven.** O `ojdbc17` é uma dependência declarada no `pom.xml`; o `lib/ojdbc17.jar` que a Sprint 3 versionava foi eliminado.
- **Maven Wrapper** (`mvnw` / `mvnw.cmd`) versionado, para que o projeto rode em qualquer máquina com Java 17, mesmo sem Maven instalado.

---

## Estrutura do repositório

```
.
├── pom.xml                              # Maven + Spring Boot 4.1.1 + Java 17   [S4]
├── mvnw, mvnw.cmd, .mvn/                # Maven Wrapper                         [S4]
├── .gitignore, .gitattributes
├── README.md                            # este arquivo (Sprint 4)
├── sql/
│   ├── create_tables.sql                # schema original                       [S3]
│   ├── insert_data.sql                  # dados originais                       [S3]
│   ├── 01_create_tables_sprint4.sql     # schema do zero (tabelas + sequences)  [S4]
│   ├── 02_migracao_sprint3_para_sprint4.sql  # IDENTITY -> SEQUENCE             [S4]
│   └── 03_insert_data_sprint4.sql       # dados de teste                        [S4]
├── docs/
│   ├── README-sprints-1-3.md            # documentação das sprints anteriores   [S3]
│   ├── enunciado-sprint4.pdf            # requisitos desta sprint
│   ├── exemplos-curl.sh                 # roteiro de requisições executável     [S4]
│   └── evidencias-requisicoes.md        # 23 requisições reais registradas      [S4]
├── lib/ojdbc17.jar                      # driver manual                         [S3]
└── src/
    ├── dao/, db/, service/, model/      # protótipo de console com JDBC puro    [S3]
    ├── main/Main.java                   # menu de console                       [S3]
    ├── main/java/br/com/motiva/         # API REST: model, repository, service,
    │                                    #   controller, dto, exception          [S4]
    ├── main/resources/
    │   ├── application.properties       # Oracle FIAP (perfil padrão)           [S4]
    │   └── application-demo.properties  # H2 em memória (demonstração)          [S4]
    └── test/
        ├── java/br/com/motiva/MotivaApiIntegrationTest.java                     [S4]
        └── resources/application-test.properties                                [S4]
```

`[S3]` = herdado das Sprints 1 a 3, preservado sem alterações. `[S4]` = criado nesta sprint.

### Abrindo o projeto na IDE

Como o repositório passou a ser um projeto Maven, importe-o pelo **`pom.xml`**, e não como projeto Java simples:

- **IntelliJ IDEA:** `File > Open` e selecione o `pom.xml` → *Open as Project*
- **VS Code:** extensão *Extension Pack for Java*, que detecta o `pom.xml` sozinho
- **Eclipse:** `File > Import > Existing Maven Projects`

---

## Referências

- Aula 13 — Spring Boot + JPA: configuração, `@Entity`, Repository, Service, Controller e cURL
- [Spring Initializr](https://start.spring.io)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
