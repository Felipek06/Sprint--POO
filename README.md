# Sprint-1---POO

Sistema de Monitoramento e Priorização de Roçada de Vegetação em Rodovias

### Objetivo da Sprint

Modelar o trecho da rodovia e as equipes de manutenção, produzindo um protótipo em console que instancia diferentes trechos, registra níveis simulados de crescimento de vegetação e associa uma equipe de manutenção a um trecho crítico.

### Classes produzidas
## model.TrechoRodovia

Representa um segmento de rodovia com controle de vegetação. Atributos:
•	quilometroInicial — km de início do trecho (>= 0)
•	quilometroFinal — km de fim do trecho (> quilometroInicial)
•	nivelVegetacaoCm — altura atual da vegetação em cm (>= 0)
•	equipeResponsavel — equipe designada ao trecho (opcional)

Comportamentos:
•	registrarCrescimento(double taxaCm) — incrementa o nível de vegetação
•	associarEquipe(model.EquipeManutencao equipe) — vincula uma equipe ao trecho
•	isCritico() — retorna true se nivelVegetacaoCm >= 50 cm


## model.EquipeManutencao
Representa uma equipe responsável pela roçada. Atributos:
•	nome — identificador da equipe (não vazio)
•	quantidadeIntegrantes — número de membros (>= 1)

### Perguntas de Reflexão
## 1. Por que model.TrechoRodovia é uma classe e "BR-116 KM 10 ao 15" é um objeto?
Classe é o molde — ela define quais atributos e comportamentos um trecho de rodovia pode ter: quilômetro inicial, quilômetro final, nível de vegetação, métodos de crescimento, etc. A classe não existe na memória como dado concreto; ela é uma descrição.

Objeto é uma instância concreta desse molde, com valores reais ocupando espaço na memória. Quando escrevemos new model.TrechoRodovia(10, 15, 5.0), estamos criando o objeto "BR-116 KM 10 ao 15" — um trecho específico, com dados reais, que pode registrar crescimento e ser associado a uma equipe.

Analogia: a planta baixa de uma casa é a classe; a casa construída no terreno é o objeto. Podem existir várias casas (objetos) feitas a partir da mesma planta (classe).

## 2. Como um método difere de uma função solta em programação estruturada?
Em programação estruturada, uma função existe de forma independente e recebe tudo que precisa por parâmetro. Não há vínculo com estado: ela opera sobre os dados que chegam e devolve um resultado, sem pertencer a ninguém.

Um método pertence a um objeto e opera diretamente sobre o estado interno dele. registrarCrescimento(5.0) acessa this.nivelVegetacaoCm daquele trecho específico sem que o chamador precise passar esse valor como argumento — o objeto já sabe quem ele é.

Isso traz duas vantagens práticas: o código que chama o método não precisa conhecer os detalhes internos do objeto (encapsulamento), e é impossível aplicar a lógica sobre dados de outro trecho por engano, já que o método só age sobre si mesmo.

## Se nivelVegetacao fosse público, que problema causaria?
Com o atributo público, qualquer linha de código em qualquer lugar do sistema poderia escrever diretamente:

trecho.nivelVegetacaoCm = -999;

Esse valor atravessaria todas as camadas sem nenhuma validação. O sistema de priorização de roçada receberia uma altura de vegetação impossível, geraria (ou deixaria de gerar) alertas críticos com dados corrompidos.

O problema mais grave não é o valor em si — é que o ponto de corrupção estaria longe do ponto de falha. A quebra aconteceria silenciosamente numa atribuição qualquer; o sintoma apareceria muito depois, num relatório de prioridade ou numa equipe despachada para o lugar errado. Com o atributo privado e a validação no método, o erro explode imediatamente onde o dado inválido é inserido, facilitando muito o diagnóstico.

### Testes Unitários cobertos no main.Main

•	Teste 1 — Instanciação válida de dois trechos (objeto não nulo)
•	Teste 2 — Crescimento válido: 10 cm + 5 cm = 15 cm
•	Teste 3 — Taxa de crescimento negativa é rejeitada
•	Teste 4 — Nível de vegetação inicial negativo é rejeitado
•	Teste 5 — KM final menor ou igual ao inicial é rejeitado
•	Teste 6 — KM inicial negativo é rejeitado
•	Teste 7 — Equipe é associada corretamente a trecho crítico (>= 50 cm)
•	Teste 8 — Equipe com nome vazio é rejeitada
•	Teste 9 — Equipe com zero integrantes é rejeitada
•	Teste 10 — Associar equipe nula ao trecho é rejeitado

### Decisões de Clean Code
•	Nomes expressivos: classes com substantivos (model.TrechoRodovia, model.EquipeManutencao), métodos com verbos no infinitivo (registrarCrescimento, associarEquipe).
•	Exceções em vez de prints: IllegalArgumentException permite que o chamador decida como tratar o erro.
•	Validações privadas isoladas: cada regra de domínio vive em seu próprio método privado, mantendo o construtor limpo.
•	Sem setters públicos desnecessários: o estado só muda por métodos de domínio com semântica clara.

# Sprint 2: O Motor de Regras

> *Sistema de Monitoramento e Priorização de Roçada de Vegetação em Rodovias*


## Objetivo da Sprint

Criar o motor de inteligência do sistema: diferentes comportamentos de crescimento de vegetação por tipo de terreno, tipos distintos de intervenção operacional e um algoritmo que varre um array de trechos e gera um **Relatório de Prioridade** automático indicando quais KMs precisam de roçada mecanizada, pulverização ou apenas monitoramento.


## Evolução em relação à Sprint 1

- **`model.TrechoRodovia` tornou-se abstrata** — não faz sentido instanciar um trecho sem tipo de terreno definido.
- Dois novos tipos concretos: `model.TrechoUmido` (cresce ~3,5 cm/dia) e `model.TrechoSeco` (cresce ~1,2 cm/dia).
- Novo método `simularCrescimento(int dias)` usa a taxa própria de cada subclasse — polimorfismo em ação.
- Todas as classes da Sprint 1 são retrocompatíveis; `model.EquipeManutencao` não sofreu alteração.
---

## Arquitetura — Classes e Interfaces

| Arquivo | Tipo | Responsabilidade |
|---|---|---|
| `model.TrechoRodovia` | Classe Abstrata | Modelo base de todos os trechos |
| `model.TrechoUmido` | Subclasse concreta | Crescimento acelerado por umidade |
| `model.TrechoSeco` | Subclasse concreta | Crescimento reduzido, estação seca |
| `model.TrechoUmidoMonitorado` | Subclasse + Interface | Úmido com sensor IoT instalado |
| `model.IntervencaoOperacional` | Classe Abstrata | Base de todas as intervenções |
| `model.RocadaMecanizada` | Subclasse concreta | Intervenção com trator roçadeira |
| `model.Pulverizacao` | Subclasse concreta | Herbicida / regulador de crescimento |
| `model.MonitoravelViaIoT` | Interface | Contrato de transmissão de sensores |
| `service.GeradorRelatorio` | Classe de serviço | Motor do relatório de prioridade |
| `model.EquipeManutencao` | Classe concreta (S1) | Herdada da Sprint 1, sem alterações |
 
---

## Perguntas de Reflexão

### 1. Por que não faz sentido executar uma "Intervenção Operacional" genérica?

No domínio da Motiva, toda ordem de serviço precisa especificar exatamente o que será executado: equipamento, produto, procedimento e custo variam completamente entre uma roçada mecanizada e uma pulverização herbicida. Uma "intervenção genérica" não carrega nenhuma dessas informações — ela é apenas um conceito, não uma ação real.

**A classe abstrata força esse contrato em tempo de compilação.** Tentar escrever `new model.IntervencaoOperacional(trecho, equipe)` não compila. O desenvolvedor é obrigado a escolher `model.RocadaMecanizada` ou `model.Pulverizacao` — ou criar uma nova subclasse concreta para um serviço ainda não mapeado.

> Analogia do domínio: um gestor de campo não despacha uma equipe para fazer "alguma coisa" no KM 42. Ele emite uma OS de roçada mecanizada ou de pulverização. A abstração no código reflete essa realidade operacional.
 
---

### 2. Diferença arquitetural: herdar classe abstrata vs. implementar interface

**Herança (`extends` classe abstrata)** define *o que o objeto é* — sua identidade e tipo na hierarquia. `model.TrechoUmido extends model.TrechoRodovia` significa que um trecho úmido *é um* trecho de rodovia, compartilha todos os seus atributos e comportamentos, e só pode ter um pai (Java não tem herança múltipla).

**Interface (`implements`)** define *o que o objeto sabe fazer* — uma capacidade adicional desacoplada da hierarquia. `model.TrechoUmidoMonitorado implements model.MonitoravelViaIoT` significa que esse trecho *sabe transmitir dados de sensor*, mas isso não muda sua identidade como `model.TrechoRodovia`. Amanhã, um `model.TrechoSeco` também pode ganhar sensor sem mudar sua hierarquia — basta implementar a mesma interface.

A regra prática para decidir:

| Situação | Usar |
|---|---|
| "X **é um** Y" | `extends` (herança) |
| "X **sabe fazer** Y" | `implements` (interface) |

**Benefício arquitetural chave:** o `service.GeradorRelatorio` pode chamar `sensor.transmitirDadosSensor()` em qualquer objeto que implemente `model.MonitoravelViaIoT` — seja trecho úmido, seco, urbano ou um mock de teste — sem conhecer a classe concreta. Isso é o desacoplamento que o Interface Segregation Principle promove.
 
---

## Lógica do Relatório de Prioridade

O `service.GeradorRelatorio` classifica cada trecho em quatro faixas:

| Nível (cm) | Prioridade | Intervenção recomendada |
|---|---|---|
| >= 80 cm | 🔴 URGENTE | Roçada Mecanizada — despachar equipe imediatamente |
| >= 50 cm | 🟠 CRÍTICO | Pulverização herbicida + reavaliar em 7 dias |
| >= 25 cm | 🟡 ATENÇÃO | Agendar roçada manual nas próximas 2 semanas |
| < 25 cm | 🟢 NORMAL | Monitoramento de rotina |

Para trechos `model.MonitoravelViaIoT`, o relatório consulta `transmitirDadosSensor()` antes de classificar, atualizando o nível automaticamente sem necessidade de inspeção visual.
 
---

## Testes cobertos no main.Main

| # | Cenário | O que valida |
|---|---|---|
| 1 | Polimorfismo de crescimento | `model.TrechoUmido` cresce mais que `model.TrechoSeco` no mesmo período |
| 2 | Abstrações não instanciáveis | Reflexão confirma que `model.TrechoRodovia` e `model.IntervencaoOperacional` são abstratas |
| 3 | Contrato IoT | Apenas `model.TrechoUmidoMonitorado` implementa `model.MonitoravelViaIoT` |
| 4 | Mock IoT | Objeto anônimo implementa a interface e retorna leitura determinística |
| 5 | Relatório completo | Array de 6 trechos gera relatório com classificação e resumo executivo |
| 5b | Execução de intervenções | `model.RocadaMecanizada` e `model.Pulverizacao` executam sobre trechos urgente e crítico |
 
---

## Decisões de Clean Code

- **Classes abstratas com `protected`:** o construtor de `model.TrechoRodovia` é `protected` — impede instanciação direta mesmo por reflexão.
- **Interface enxuta (ISP):** `model.MonitoravelViaIoT` tem apenas 2 métodos. Nenhuma responsabilidade de trecho ou equipe vazou para ela.
- **Enum interno em `model.Pulverizacao`:** `TipoProduto` torna o tipo de produto explícito e seguro em vez de usar strings livres.
- **Enum privado em `service.GeradorRelatorio`:** `Prioridade` encapsula a lógica de classificação dentro do gerador, sem expor ao restante do sistema.
- **Pattern matching (`instanceof`):** uso de `trecho instanceof model.MonitoravelViaIoT sensor` (Java 16+) evita cast explícito e torna o código mais seguro e legível.
- **Método template protegido:** `imprimirCabecalhoExecucao()` em `model.IntervencaoOperacional` padroniza a saída de todas as subclasses sem duplicar código.


---

# Sprint 3: A Camada de Persistência

> *Sistema de Monitoramento e Priorização de Roçada de Vegetação em Rodovias*


## Objetivo da Sprint

Conectar o domínio modelado nas sprints anteriores a um banco de dados real. Toda entidade do sistema — equipes, trechos, intervenções e relatórios — passa a ser persistida em Oracle via **JDBC puro**, sem ORM. O padrão DAO isola o acesso ao banco do domínio, e o Singleton garante uma única conexão ativa em toda a aplicação.


## Evolução em relação à Sprint 2

- As classes de domínio (`model.TrechoRodovia`, `model.EquipeManutencao`, `model.IntervencaoOperacional`) ganharam campo `id` hidratado pelo banco após persistência.
- `service.GeradorRelatorio` passou a persistir o resultado de cada relatório em `RELATORIO_PRIORIDADE`.
- Quatro novas classes DAO foram criadas, cada uma responsável pelo CRUD de uma tabela Oracle.
- `db.ConexaoBD` foi introduzida como Singleton que gerencia a conexão JDBC durante toda a execução.
- Toda a hierarquia da Sprint 2 permanece funcionando sem alterações nas regras de negócio.

---

## Arquitetura — Camadas e Responsabilidades

| Arquivo | Camada | Tipo | Responsabilidade |
|---|---|---|---|
| `db.ConexaoBD` | Banco | Singleton | Única conexão ativa com o Oracle |
| `dao.EquipeManutencaoDAO` | DAO | DAO | CRUD de equipes |
| `dao.TrechoRodoviaDAO` | DAO | DAO + STI | CRUD de trechos (3 subtipos numa tabela) |
| `dao.IntervencaoOperacionalDAO` | DAO | DAO | Histórico de intervenções executadas |
| `dao.RelatorioPrioridadeDAO` | DAO | DAO | Snapshots de relatórios gerados |
| `service.GeradorRelatorio` | Serviço | Serviço | Motor de priorização + persistência de histórico |
| Classes `model.*` | Domínio | Herança/Interface | Regras de negócio (herdadas das sprints 1 e 2) |
| `main.Main` | Entrada | Orquestrador | Demonstra o ciclo completo via DAO |

---

## Padrões Implementados

| Padrão | Onde é Aplicado |
|---|---|
| **Singleton** | `db.ConexaoBD` — construtor privado, lazy initialization, `synchronized` |
| **Data Access Object (DAO)** | Uma classe por tabela, CRUD completo com `PreparedStatement` |
| **Single Table Inheritance (STI)** | `TRECHO_RODOVIA` discriminada por `TIPO_TRECHO`; reconstrução polimórfica no DAO |
| **Template Method** | `imprimirCabecalhoExecucao()` em `IntervencaoOperacional` (herdado da Sprint 2) |
| **Strategy** | `calcularTaxaCrescimentoDiario()` por subtipo de trecho (herdado da Sprint 2) |
| **DTO via record** | `EquipeManutencaoRow`, `TrechoRodoviaRow`, `IntervencaoRegistrada`, `RelatorioRegistrado` |

---

## Perguntas de Reflexão

### 1. Por que usar o padrão DAO em vez de colocar o SQL direto nas classes de domínio?

Uma classe de domínio como `model.TrechoRodovia` carrega regras de negócio: crescimento de vegetação, limiar crítico, associação de equipe. Ela não sabe — e não deve saber — se os dados vêm de um banco Oracle, de um arquivo JSON ou de um mock de teste.

Se o SQL estivesse dentro de `TrechoRodovia`, uma mudança de banco de dados (de Oracle para PostgreSQL, por exemplo) exigiria mexer na classe de domínio, arriscando quebrar as regras de negócio. Com o DAO, a única classe que muda é `TrechoRodoviaDAO` — o domínio nem percebe.

**O DAO é a fronteira entre o mundo dos objetos e o mundo das tabelas.** Ele traduz nos dois sentidos: objeto → INSERT/UPDATE e ResultSet → objeto reconstituído.

---

### 2. O que é Single Table Inheritance e por que foi escolhido para `TrechoRodovia`?

A hierarquia `TrechoRodovia` tem três tipos concretos: `TrechoSeco`, `TrechoUmido` e `TrechoUmidoMonitorado`. Existem três estratégias para persistir isso:

| Estratégia | Como funciona | Quando usar |
|---|---|---|
| **Single Table (STI)** | Uma tabela com todos os campos; colunas não aplicáveis ficam nulas | Hierarquia pequena e estável |
| **Table per Class** | Uma tabela por subclasse concreta | Muitas diferenças entre subclasses |
| **Joined Table** | Tabela pai + tabela filha por subclasse | Hierarquia grande e com muitos campos distintos |

STI foi escolhido porque os três tipos compartilham a maioria dos campos e diferem em apenas um ou dois atributos (`indicePluviometrico`, `emEstacaoSeca`, `idSensor`). Uma única tabela significa JOIN zero ao buscar trechos — a query mais frequente do sistema.

O custo é aceitar colunas nulas: `INDICE_PLUVIOMETRICO` é nulo em `SECO`, `EM_ESTACAO_SECA` é nulo em `UMIDO`, e assim por diante. A coluna `TIPO_TRECHO` atua como discriminador, e o DAO usa um `switch` para reconstituir o objeto concreto correto.

---

## Fluxo de Persistência

O `main.Main` demonstra o ciclo completo em cinco fases:

1. **Conexão** — `ConexaoBD.getInstancia().conectar()` abre a única conexão Oracle
2. **CRUD de Equipes** — insere Alpha e Beta, busca, lista, atualiza, deleta
3. **CRUD de Trechos** — cria 5 trechos de subtipos diferentes, simula crescimento, persiste e lista
4. **Intervenções e Relatório** — executa `RocadaMecanizada` e `Pulverizacao`, registra no banco, gera relatório (que é automaticamente salvo em `RELATORIO_PRIORIDADE`)
5. **Histórico** — lista todos os relatórios persistidos e exibe os snapshots

---

## Modelo de Dados

```sql
EQUIPE_MANUTENCAO
  ID                        NUMBER (PK, GENERATED ALWAYS AS IDENTITY)
  NOME                      VARCHAR2(100) NOT NULL
  QUANTIDADE_INTEGRANTES    NUMBER(3) CHECK >= 1

TRECHO_RODOVIA                            -- STI: três subtipos em uma tabela
  ID                        NUMBER (PK)
  QUILOMETRO_INICIAL        NUMBER(6)  CHECK >= 0
  QUILOMETRO_FINAL          NUMBER(6)  CHECK > QUILOMETRO_INICIAL
  NIVEL_VEGETACAO_CM        NUMBER(7,2) CHECK >= 0
  TIPO_TRECHO               VARCHAR2(30) IN ('UMIDO', 'SECO', 'UMIDO_MONITORADO')
  INDICE_PLUVIOMETRICO      NUMBER(4,2)   -- nulo em SECO
  EM_ESTACAO_SECA           CHAR(1)       -- nulo em UMIDO e UMIDO_MONITORADO
  ID_SENSOR                 VARCHAR2(50)  -- nulo em UMIDO e SECO
  ID_EQUIPE_RESPONSAVEL     FK → EQUIPE_MANUTENCAO

INTERVENCAO_OPERACIONAL
  ID                        NUMBER (PK)
  ID_TRECHO_ALVO            FK → TRECHO_RODOVIA  NOT NULL
  ID_EQUIPE_RESPONSAVEL     FK → EQUIPE_MANUTENCAO NOT NULL
  TIPO_INTERVENCAO          VARCHAR2(30) IN ('ROCADA_MECANIZADA', 'PULVERIZACAO')
  TIPO_PRODUTO              VARCHAR2(30)  -- nulo em ROCADA_MECANIZADA
  DATA_EXECUCAO             TIMESTAMP DEFAULT SYSTIMESTAMP

RELATORIO_PRIORIDADE
  ID                        NUMBER (PK)
  DATA_GERACAO              TIMESTAMP DEFAULT SYSTIMESTAMP
  QT_URGENTE                NUMBER(5)
  QT_CRITICO                NUMBER(5)
  QT_ATENCAO                NUMBER(5)
  QT_NORMAL                 NUMBER(5)
  RESUMO                    VARCHAR2(4000)
```

---

## Testes cobertos no main.Main

| # | Cenário | O que valida |
|---|---|---|
| 1 | Singleton de conexão | `ConexaoBD.getInstancia()` retorna a mesma referência em chamadas sucessivas |
| 2 | CRUD de equipe | Insere, busca por ID, lista, atualiza e deleta `EquipeManutencao` com round-trip ao Oracle |
| 3 | Inserção polimórfica de trecho | `TrechoRodoviaDAO.inserir()` identifica o subtipo e preenche colunas específicas corretamente |
| 4 | Reconstituição polimórfica | `buscarPorId()` retorna `TrechoUmidoMonitorado`, `TrechoUmido` ou `TrechoSeco` correto com base em `TIPO_TRECHO` |
| 5 | Hidratação de equipe no trecho | Trecho buscado do banco carrega `EquipeManutencao` associada (FK resolvida pelo DAO) |
| 6 | Registro de `RocadaMecanizada` | `IntervencaoOperacionalDAO.inserir()` grava `TIPO_INTERVENCAO = 'ROCADA_MECANIZADA'` e `TIPO_PRODUTO` nulo |
| 7 | Registro de `Pulverizacao` | Grava `TIPO_INTERVENCAO = 'PULVERIZACAO'` e `TIPO_PRODUTO = 'HERBICIDA_SELETIVO'` |
| 8 | Persistência do relatório | `GeradorRelatorio.gerarRelatorio()` salva snapshot automaticamente em `RELATORIO_PRIORIDADE` |
| 9 | Histórico de relatórios | `RelatorioPrioridadeDAO.listarTodas()` retorna os snapshots salvos com data e contagens |
| 10 | Desconexão limpa | `ConexaoBD.desconectar()` fecha a conexão sem erro mesmo com múltiplas operações anteriores |

---

## Decisões de Clean Code

- **Credenciais via variáveis de ambiente:** `ConexaoBD` lê `ORACLE_USER` e `ORACLE_PASSWORD` do ambiente — nenhuma senha no código-fonte.
- **`try-with-resources` em todo DAO:** `PreparedStatement` e `ResultSet` são sempre fechados, mesmo em caso de exceção, eliminando vazamento de cursores Oracle.
- **Queries como constantes:** cada SQL vive numa constante `private static final String SQL_*`, tornando as queries visíveis e fáceis de alterar sem tocar na lógica.
- **Records internos como DTOs de linha:** `EquipeManutencaoRow`, `TrechoRodoviaRow` capturam o estado cru do `ResultSet` antes de qualquer lógica — separa o mapeamento da reconstituição.
- **IDs gerados via `getGeneratedKeys()`:** nenhum DAO executa `SELECT MAX(ID)` após inserção — usa o mecanismo seguro do JDBC para recuperar a chave gerada.
- **STI com NULL explícito via `Types.*`:** colunas inaplicáveis recebem `setNull(n, Types.NUMERIC)` em vez de zero, preservando a semântica do NULL no banco.
- **`salvarHistorico()` silencioso:** falha de persistência do relatório é logada mas não interrompe a exibição — o usuário recebe o relatório mesmo se o banco estiver indisponível.


---

# Sprint 4: Spring Boot + JPA + API REST

> *Sistema de Monitoramento e Priorização de Roçada de Vegetação em Rodovias*


## Objetivo da Sprint

Transformar o protótipo de console da Sprint 3 em uma **API REST**, substituindo o JDBC puro e o pattern DAO escrito à mão por Spring Boot e Spring Data JPA — reaproveitando o mesmo banco Oracle e as mesmas tabelas da sprint anterior.

```java
// Sprint 3 (JDBC puro): ~40 linhas por operação
Connection conn = ConexaoBD.getInstancia().conectar();
PreparedStatement stmt = conn.prepareStatement("INSERT INTO ...");
// ... parâmetros, execute, getGeneratedKeys, fechar tudo ...

// Sprint 4 (Spring Data JPA): 1 linha
repository.save(trecho);
```


## Evolução em relação à Sprint 3

| Sprint 3 (JDBC puro) | Sprint 4 (Spring Boot) | Papel |
|---|---|---|
| `model/` + `record` interno do DAO | `@Entity` em `model/` | Classe mapeada para a tabela via JPA |
| `EquipeManutencaoDAO` (136 linhas) | `EquipeManutencaoRepository` (interface, sem corpo) | Acesso a dados sem SQL manual |
| `db.ConexaoBD` (Singleton) | `application.properties` + pool HikariCP | Conexão gerenciada pelo framework |
| `service.GeradorRelatorio` | `RelatorioPrioridadeService` (`@Service`) | Regras de negócio — continua existindo |
| `Main.main()` com `System.out.println` | `@RestController` + JSON | Interface do sistema vira HTTP |
| Teste manual no console | cURL / Postman + `@SpringBootTest` | A API é consumida por qualquer cliente |

**O pattern DAO não morreu — foi absorvido.** O `Repository` do Spring Data é um DAO gerado pelo framework em tempo de execução. O que desapareceu foi a *implementação manual* do padrão.

O código novo não tem `Connection`, `PreparedStatement`, `ResultSet`, SQL manual, `System.out.println` como saída, nem lógica de negócio em Controller. Não existe um único `@Query` no projeto — todas as consultas são *derived queries*.

> O código das Sprints 1 a 3 continua versionado aqui, intocado, em `src/dao`, `src/db`, `src/model`, `src/service` e `src/main/Main.java`. Como o Maven compila apenas `src/main/java`, as duas versões convivem sem conflito de build.


## Arquitetura — Camadas Spring

```
src/main/java/br/com/motiva/
├── MotivaApplication.java   # @SpringBootApplication
├── model/                   # @Entity — as entidades da Sprint 3
│   ├── TrechoRodovia + TrechoUmido / TrechoSeco / TrechoUmidoMonitorado
│   ├── IntervencaoOperacional + RocadaMecanizada / Pulverizacao
│   ├── EquipeManutencao, RelatorioPrioridade, MonitoravelViaIoT
│   └── Prioridade (enum com os limiares da Sprint 2)
├── repository/              # Interfaces JpaRepository — SEM SQL
├── service/                 # @Service — regras de negócio
├── controller/              # @RestController — endpoints REST
├── dto/                     # Contratos de entrada e saída da API
└── exception/               # @RestControllerAdvice + exceções de domínio
```

**Fluxo:** `Controller → Service → Repository → Banco`. Cada camada só conversa com a vizinha.

### A Single Table Inheritance da Sprint 3 virou herança de banco

Na Sprint 3, o `TrechoRodoviaDAO` resolvia a herança na unha: lia `TIPO_TRECHO`, abria um `switch` e chamava `new TrechoUmido(...)`, `new TrechoSeco(...)` ou `new TrechoUmidoMonitorado(...)`.

Na Sprint 4 esse `switch` sumiu. `@Inheritance(SINGLE_TABLE)` declara que a hierarquia mora em uma tabela e `@DiscriminatorColumn` aponta qual coluna guarda o tipo — a partir daí é o Hibernate que instancia a subclasse certa. A modelagem do banco é exatamente a mesma.


## Como Executar

**Pré-requisitos:** Java 17 e acesso ao Oracle da FIAP com as tabelas da Sprint 3.

**1. Migrar o banco.** A Sprint 3 gerava o ID com `GENERATED ALWAYS AS IDENTITY`; a Sprint 4 exige `@SequenceGenerator`, e o Oracle recusa `INSERT` com ID explícito em coluna identity (ORA-32795). Rode uma vez:

```sql
@sql/02_migracao_sprint3_para_sprint4.sql
```

O script é idempotente e **preserva os dados**: troca a identity por uma sequence começando em `MAX(ID) + 1`. Para montar o schema do zero, use `01_create_tables_sprint4.sql` e `03_insert_data_sprint4.sql`.

**2. Definir as credenciais.** Nenhuma senha fica no código nem no `application.properties` — ele lê `ORACLE_USER` e `ORACLE_PASSWORD` do ambiente, mantendo a prática que a `ConexaoBD` já adotava:

```powershell
$env:ORACLE_USER = "seu_rm"         # PowerShell
$env:ORACLE_PASSWORD = "sua_senha"
```

```bash
export ORACLE_USER=seu_rm           # Linux / macOS
export ORACLE_PASSWORD=sua_senha
```

A URL padrão é a do Oracle da FIAP, mas a variável `ORACLE_URL` aponta o projeto para qualquer instância sem tocar no código:

```bash
# Oracle da FIAP por SERVICE_NAME em vez de SID
export ORACLE_URL='jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL'

# Oracle XE local — foi contra esta instância que as evidências desta
# entrega foram geradas, porque é onde vivem as tabelas da Sprint 3.
# No XE 21c o serviço é o PDB XEPDB1, e não XE.
export ORACLE_URL='jdbc:oracle:thin:@//localhost:1521/XEPDB1'
```

**3. Subir a aplicação** em <http://localhost:8080>:

```bash
./mvnw spring-boot:run
```

O repositório traz o Maven Wrapper, então não é preciso ter Maven instalado. Para demonstrar sem o Oracle, existe o perfil `demo` com H2 em memória: `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo`.

> O projeto agora é Maven — importe pelo **`pom.xml`**, não como projeto Java simples.


## Endpoints

| Método | Rota | Ação | Respostas |
|---|---|---|---|
| `GET` | `/api/trechos` | Listar todos | `200` |
| `GET` | `/api/trechos?tipo=SECO` | Filtrar por tipo (*derived query*) | `200` |
| `GET` | `/api/trechos?nivelMinimoCm=50` | Filtrar por nível (*derived query*) | `200` |
| `GET` | `/api/trechos/{id}` | Buscar por ID | `200` / `404` |
| `POST` | `/api/trechos` | Criar | `201` + `Location` / `400` |
| `PUT` | `/api/trechos/{id}` | Atualizar | `200` / `404` / `400` |
| `POST` | `/api/trechos/{id}/simulacao?dias=10` | Simular crescimento (Sprint 2) | `200` / `404` |
| `DELETE` | `/api/trechos/{id}` | Remover | `204` / `404` / `400` |
| `GET` `POST` `PUT` `DELETE` | `/api/equipes` · `/api/equipes/{id}` | CRUD de equipes | idem trechos |
| `GET` `POST` `PUT` `DELETE` | `/api/intervencoes` · `/api/intervencoes/{id}` | CRUD de intervenções | idem trechos |
| `POST` | `/api/relatorios` | Gerar o relatório e gravar no histórico | `201` + `Location` / `400` |
| `GET` | `/api/relatorios` | Listar o histórico | `200` |
| `GET` | `/api/relatorios/periodo?inicio=...&fim=...` | Consultar por período | `200` / `400` |

`200` consulta ou atualização · `201` criado, com `Location` · `204` removido · `400` validação ou regra de negócio · `404` ID inexistente · `409` integridade do banco · `500` erro inesperado, com stack trace apenas no log.

Toda resposta de erro usa o mesmo formato:

```json
{
  "timestamp": "2026-10-09T11:01:07", "status": 400, "erro": "Bad Request",
  "mensagem": "Um ou mais campos da requisicao sao invalidos.",
  "caminho": "/api/trechos",
  "campos": { "nivelVegetacaoCm": ["O nivel de vegetacao nao pode ser negativo."] }
}
```


## Exemplos cURL

> [`docs/evidencias-requisicoes.md`](docs/evidencias-requisicoes.md) traz **23 requisições reais contra o Oracle**, com comando, código HTTP e resposta — 12× `200`, 5× `201`, 1× `204`, 3× `400` e 2× `404`. Foi gerado por [`docs/exemplos-curl.sh`](docs/exemplos-curl.sh), que pode ser reexecutado: `bash docs/exemplos-curl.sh > docs/evidencias-requisicoes.md`.

```bash
# Criar equipe -> 201 Created + Location
curl -i -X POST http://localhost:8080/api/equipes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Equipe Alpha","quantidadeIntegrantes":6}'

# Criar trecho seco em estação seca -> 201
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"SECO","quilometroInicial":30,"quilometroFinal":40,
       "nivelVegetacaoCm":88.0,"emEstacaoSeca":true}'

# Derived queries -> 200
curl -i 'http://localhost:8080/api/trechos?tipo=SECO'
curl -i 'http://localhost:8080/api/trechos?nivelMinimoCm=50'

# Registrar intervenção -> 201
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"ROCADA_MECANIZADA","trechoAlvoId":1,"equipeResponsavelId":1}'

# Relatório: gerar, listar e filtrar por período
curl -i -X POST http://localhost:8080/api/relatorios
curl -i http://localhost:8080/api/relatorios
curl -i 'http://localhost:8080/api/relatorios/periodo?inicio=2026-01-01T00:00:00&fim=2026-12-31T23:59:59'

# Erros -> 404 e 400
curl -i http://localhost:8080/api/trechos/999999
curl -i -X POST http://localhost:8080/api/trechos -H 'Content-Type: application/json' \
  -d '{"tipo":"SECO","quilometroInicial":90,"quilometroFinal":100,"nivelVegetacaoCm":-5.0}'
```

A resposta de um trecho traz campos **calculados na hora** pelo polimorfismo da Sprint 2, que não existem em coluna nenhuma — `taxaCrescimentoDiarioCm`, `critico`, `prioridade` e `recomendacao`:

```json
{
  "id": 1, "tipo": "SECO", "descricaoTipo": "Trecho Seco", "nivelVegetacaoCm": 88.0,
  "taxaCrescimentoDiarioCm": 0.72, "critico": true, "prioridade": "URGENTE",
  "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE"
}
```

Já o POST de intervenção devolve em `resultadoExecucao` o retorno polimórfico de `executarServico()` — o texto que nas Sprints 2 e 3 ia para o console: *"Roçada mecanizada concluída... Nível antes: 88,0 cm; nível após: 20,0 cm"*. A roçada **derrubou o nível do trecho para 20 cm** e isso foi gravado pelo *dirty checking* do Hibernate, sem nenhum `UPDATE` escrito à mão.


## Regras de Negócio

Todas vivem no `@Service`; nenhuma está no Controller. O motor de prioridade mantém os limiares da Sprint 2 — `≥ 80` `URGENTE`, `≥ 50` `CRITICO`, `≥ 25` `ATENCAO`, abaixo disso `NORMAL` — agora no enum `Prioridade`. Antes de classificar, o serviço consulta os sensores testando **contra a interface** `MonitoravelViaIoT`, não contra classe concreta.

O critério para decidir onde cada validação mora:

- **Bean Validation no DTO** quando dá para decidir olhando um campo isolado: `nivelVegetacaoCm >= 0`, `quantidadeIntegrantes >= 1`, `indicePluviometrico >= 1.0`.
- **Service** quando é preciso comparar dois campos (`kmFinal > kmInicial`), consultar o banco (nome de equipe duplicado, faixa de km ocupada, remoção de registro com histórico) ou saber o tipo do objeto (`idSensor` obrigatório só para `UMIDO_MONITORADO`, `tipoProduto` só para `PULVERIZACAO`).

Uma regra nossa: intervenção em trecho classificado como `NORMAL` é recusada com `400` — despachar equipe para vegetação baixa desperdiça recurso e contradiz o próprio relatório de prioridade.


## Perguntas de Reflexão

### 1. Por que o Repository é uma interface e não uma classe? Quem escreve a implementação e quando?

Porque a interface descreve **o que** queremos do banco, e isso é tudo o que precisamos escrever. O **como** — abrir conexão, montar SQL, posicionar parâmetros, percorrer o `ResultSet`, fechar tudo — é mecânico e idêntico para qualquer entidade. Era esse código repetitivo que ocupava a maior parte dos nossos DAOs: os quatro da Sprint 3 somavam 633 linhas e eram variações do mesmo esqueleto.

Quem escreve a implementação é o **Spring Data JPA**, **em tempo de execução**, quando a aplicação sobe. Ao encontrar uma interface que estende `JpaRepository`, ele gera dinamicamente um *proxy* que a implementa e o registra como bean, delegando as operações padrão ao `SimpleJpaRepository`. Não existe `TrechoRodoviaRepositoryImpl` em lugar nenhum deste projeto — e ainda assim `trechoRepository.save(trecho)` funciona. Imprimindo a classe real do bean injetado aparece algo como `jdk.proxy.$Proxy123`, e não uma classe nossa.

### 2. O pattern DAO da Sprint 3 "morreu" na migração ou só mudou de forma?

Só mudou de forma. O DAO é um padrão de **projeto**, não uma tecnologia: ele diz para isolar o acesso a dados atrás de uma interface, de modo que o resto do sistema peça objetos de domínio sem saber de onde vêm. Isso continua intacto — o `RelatorioPrioridadeService` pede `trechoRepository.findAll()` e recebe trechos prontos, exatamente como o `GeradorRelatorio` pedia `trechoDAO.listarTodas()`.

O que morreu foi a implementação manual:

| Papel do DAO | Sprint 3 | Sprint 4 |
|---|---|---|
| Contrato de acesso a dados | métodos do `TrechoRodoviaDAO` | interface `TrechoRodoviaRepository` |
| Tradução objeto ↔ linha | `mapearLinha()` escrito por nós | anotações `@Entity` / `@Column` |
| Reconstruir a subclasse certa | `switch` sobre `TIPO_TRECHO` | `@DiscriminatorValue` |
| Gerenciar conexão | `ConexaoBD` (Singleton) | pool do Spring |

O Repository do Spring Data é um DAO gerado pelo framework em tempo de execução.

### 3. Por que a validação de `nivelVegetacao >= 0` deve ficar no Service e não no Controller?

Porque é uma regra do **MOTIVA**, não um detalhe do **HTTP**. O Controller é só uma das portas de entrada possíveis — o próprio histórico deste projeto mostra isso, já que nas Sprints 1 a 3 a porta era um menu de console. Se amanhã o sistema ganhar um importador de CSV ou um job que lê sensores, todos vão chamar o Service e herdar a validação. No Controller, ela valeria só para quem entrasse pelo HTTP, e cada nova porta teria que reimplementá-la — validação duplicada é validação que uma hora diverge.

Somam-se duas razões: dá para testar a regra chamando o Service direto, sem subir servidor; e o Service fala a língua do negócio, lançando `RegraDeNegocioException`, enquanto o `@RestControllerAdvice` traduz para `400`. Assim a regra não conhece códigos HTTP e o HTTP não conhece a regra.

Na prática aplicamos isso em dois níveis: o `nivelVegetacao >= 0` está como `@PositiveOrZero` no DTO, por ser checagem de formato sobre um campo isolado; o que exige conhecer o domínio ficou no Service. Em nenhum dos casos a validação está no Controller.

### 4. No JDBC puro vocês escreviam SQL. Onde está o SQL do `findByTipo()`? Quem o gerou?

O SQL **não existe** enquanto a aplicação está parada — não está no código, em arquivo de configuração nem em `@Query`. Ele nasce quando a aplicação sobe, escrito pelo Spring Data JPA junto com o Hibernate, em três etapas:

1. O Spring Data encontra `findByTipo`, remove o prefixo `findBy` e procura um atributo `tipo` em `TrechoRodovia`. Como não há sufixo de operador (`GreaterThanEqual`, `IsNull`…), assume igualdade.
2. Monta uma consulta **JPQL**, que fala de objetos: `select t from TrechoRodovia t where t.tipo = :tipo`. Ainda não há SQL.
3. Na primeira execução, o Hibernate traduz a JPQL usando o dialeto (`OracleDialect`) e o mapeamento das anotações, descobrindo que a tabela é `TRECHO_RODOVIA` e a coluna é `TIPO_TRECHO`.

Com `spring.jpa.show-sql=true` dá para ver o resultado no console:

```sql
select t1_0.ID, t1_0.TIPO_TRECHO, t1_0.NIVEL_VEGETACAO_CM, ...
  from TRECHO_RODOVIA t1_0 where t1_0.TIPO_TRECHO = ?
```

Detalhe de implementação: `TIPO_TRECHO` é a coluna discriminadora da herança e o JPA não a expõe como atributo. Para a *derived query* funcionar, declaramos um espelho somente-leitura dela — `@Column(name = "TIPO_TRECHO", insertable = false, updatable = false)`. Sem o `insertable/updatable = false` o Hibernate acusaria dois mapeamentos disputando a mesma coluna.

Vale notar o ganho: aquele `?` é parâmetro vinculado, não concatenação de texto, então consultas geradas assim são imunes a SQL injection por construção. E, como a tradução passa pelo dialeto, a mesma `findByTipo` roda contra o Oracle e contra o H2 dos testes sem uma linha de diferença.


## Testes

Nas sprints anteriores os testes eram chamadas manuais dentro do `main.Main`. Agora são automatizados com `@SpringBootTest` + `MockMvc`:

```bash
./mvnw test     # Tests run: 22, Failures: 0, Errors: 0
```

Além disso, a aplicação foi subida uma vez contra o Oracle com `spring.jpa.hibernate.ddl-auto=validate`, e o Hibernate conferiu cada coluna, tipo e sequence contra as tabelas reais da Sprint 3 antes de aceitar iniciar. Foi essa checagem que revelou dois mapeamentos divergentes, hoje corrigidos: um `Double` ia para `BINARY_DOUBLE` quando a coluna é `NUMBER(7,2)`, e o `boolean` convertido ia para `VARCHAR2(1)` quando a coluna é `CHAR(1)`.

A suíte sobe o contexto Spring inteiro contra um H2 em memória, o que de quebra **valida o mapeamento JPA**: se um `@Column` apontasse para coluna inexistente ou um `@SequenceGenerator` estivesse mal declarado, o contexto nem subiria. Cobre o CRUD completo de equipe e trecho (`201 → 200 → 200 → 204 → 404`), o cabeçalho `Location`, o polimorfismo da taxa de crescimento das três subclasses (1,2 / 0,72 / 6,3 cm/dia), as duas *derived queries*, o efeito polimórfico de `executarServico()`, as regras que devolvem `400` e os três endpoints de relatório. Cada teste é `@Transactional`, então nada do que grava sobrevive.


## Boas Práticas de Git

- **Commits incrementais e temáticos**, um por etapa da migração, seguindo *Conventional Commits* (`feat:`, `docs:`, `test:`, `chore:`).
- **`.gitignore` adequado:** `target/`, arquivos de IDE, arquivos de sistema e logs.
- **Nenhuma credencial versionada:** usuário e senha do Oracle vêm do ambiente; o `.gitignore` também bloqueia `.env` e `application-local.properties`.
- **Driver via Maven:** o `ojdbc17` virou dependência do `pom.xml`, no lugar do `lib/ojdbc17.jar` que a Sprint 3 versionava.
- **`.gitattributes`** preserva LF no `mvnw` e nos scripts `.sh`, que senão quebrariam em Linux e macOS depois de um clone no Windows.


## Decisões de Clean Code

- **Injeção por construtor com campos `final`:** na Sprint 3 cada classe criava seus DAOs com `new`; aqui o Spring entrega as dependências prontas, e o `final` torna impossível existir um Service pela metade.
- **DTOs separando entidade e contrato:** o cliente não escolhe o próprio id, e renomear uma coluna não quebra quem consome a API.
- **Exceções de domínio no Service, não códigos HTTP:** o `@RestControllerAdvice` faz a tradução, e todas as falhas devolvem o mesmo formato JSON — stack trace vai só para o log, nunca para a resposta.
- **`BooleanSNConverter` centraliza a tradução `boolean` ↔ `CHAR(1)`:** o que estava espalhado por cada `setString`/`getString` do DAO agora existe num lugar só.
- **`allocationSize = 1` e `@Enumerated(STRING)`:** o padrão do JPA (50) geraria IDs com buracos em relação às linhas da Sprint 3, e gravar o enum como texto evita que reordená-lo um dia corrompa o histórico.
- **`FetchType.LAZY`, `open-in-view=false` e `ddl-auto=none`:** só busca o que for pedido, não deixa a sessão do JPA vazar para a serialização do JSON e não permite que o Hibernate altere o schema da Sprint 3.
