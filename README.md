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

Transformar o protótipo de console da Sprint 3 em uma **API REST profissional**, substituindo o JDBC puro e o pattern DAO escrito à mão por Spring Boot e Spring Data JPA — reaproveitando o mesmo banco Oracle e as mesmas tabelas modeladas na sprint anterior.

A mensagem da sprint cabe em duas linhas:

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
| `EquipeManutencaoDAO` (136 linhas) | `EquipeManutencaoRepository` (interface, 0 linhas de corpo) | Acesso a dados sem SQL manual |
| `db.ConexaoBD` (Singleton) | `application.properties` + pool HikariCP | Conexão gerenciada pelo framework |
| `service.GeradorRelatorio` | `RelatorioPrioridadeService` (`@Service`) | Regras de negócio — continua existindo |
| `Main.main()` com `System.out.println` | `@RestController` + JSON | Interface do sistema vira HTTP |
| Teste manual no console | cURL / Postman + `@SpringBootTest` | A API é consumida por qualquer cliente |

**O pattern DAO não morreu — ele foi absorvido.** O `Repository` do Spring Data é um DAO gerado pelo framework em tempo de execução. O que desapareceu foi a *implementação manual* do padrão, não o padrão.

### O que foi eliminado nesta sprint

- `Connection`, `PreparedStatement` e `ResultSet` no código novo;
- SQL manual dentro de `@Service` ou `@RestController` — não existe um único `@Query` no projeto, todas as consultas são *derived queries*;
- lógica de negócio dentro de `@RestController`;
- `System.out.println` como interface do sistema — a saída agora é JSON.

> O código das Sprints 1 a 3 continua versionado neste repositório, intocado, em `src/dao`, `src/db`, `src/model`, `src/service` e `src/main/Main.java`. Como o Maven compila apenas `src/main/java`, as duas versões convivem sem conflito de build: o `grep` abaixo não retorna nenhuma linha do projeto Spring Boot.
>
> ```bash
> grep -rn "PreparedStatement\|ResultSet\|System.out.print" src/main/java | grep -v '\*'
> ```


## Arquitetura — Camadas Spring

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

### A herança da Sprint 2 virou herança de banco

Na Sprint 3, o `TrechoRodoviaDAO` resolvia a Single Table Inheritance na unha: lia a coluna `TIPO_TRECHO`, abria um `switch` e chamava `new TrechoUmido(...)`, `new TrechoSeco(...)` ou `new TrechoUmidoMonitorado(...)` conforme o texto lido.

Na Sprint 4 esse `switch` sumiu. `@Inheritance(SINGLE_TABLE)` declara que a hierarquia inteira mora em uma tabela, e `@DiscriminatorColumn` aponta qual coluna guarda o tipo. A partir daí é o próprio Hibernate que lê `TIPO_TRECHO` e instancia a subclasse certa — a mesma lógica, escrita uma vez por quem fez o framework em vez de uma vez por projeto.

| Classe | Discriminador | Taxa de crescimento |
|---|---|---|
| `TrechoUmido` | `UMIDO` | 3,5 cm/dia × índice pluviométrico |
| `TrechoSeco` | `SECO` | 1,2 cm/dia (×0,6 em estação seca) |
| `TrechoUmidoMonitorado` | `UMIDO_MONITORADO` | herda de `TrechoUmido` + sensor IoT |


## Como executar

### Pré-requisitos

- **Java 17** (o projeto não compila em versões anteriores)
- **Maven** — opcional: o repositório traz o Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Acesso ao **Oracle da FIAP** com as tabelas da Sprint 3 criadas

### Passo 1 — Migrar o banco da Sprint 3

A Sprint 3 gerava o ID com `GENERATED ALWAYS AS IDENTITY`. A Sprint 4 exige `@GeneratedValue` com `@SequenceGenerator` ligado a *sequences*, e o Oracle recusa qualquer `INSERT` que informe o ID de uma coluna `GENERATED ALWAYS` (ORA-32795). Por isso a identity precisa sair e dar lugar a uma sequence.

Conecte com o mesmo usuário da Sprint 3 e rode:

```sql
@sql/02_migracao_sprint3_para_sprint4.sql
```

O script é idempotente e **preserva todos os dados**: remove a identity de cada tabela e cria a sequence correspondente começando em `MAX(ID) + 1`, para não colidir com as linhas já gravadas.

| Arquivo | Quando usar |
|---|---|
| `sql/create_tables.sql` · `sql/insert_data.sql` | Scripts originais da Sprint 3, mantidos como registro |
| `sql/01_create_tables_sprint4.sql` | Schema vazio — cria tabelas e sequences do zero |
| `sql/02_migracao_sprint3_para_sprint4.sql` | **Caso comum** — você já rodou a Sprint 3 e quer manter os dados |
| `sql/03_insert_data_sprint4.sql` | Dados de teste, só depois do script 01 |

### Passo 2 — Definir as credenciais

Nenhuma senha fica no código nem no `application.properties`: o projeto lê as variáveis de ambiente `ORACLE_USER` e `ORACLE_PASSWORD`, mantendo a mesma prática que a `ConexaoBD` já adotava na Sprint 3.

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

**IntelliJ IDEA:** `Run > Edit Configurations > Environment variables` → `ORACLE_USER=rm564878;ORACLE_PASSWORD=sua_senha`

Se o laboratório usar `SERVICE_NAME` em vez de `SID`, defina também:

```bash
export ORACLE_URL='jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL'
```

### Passo 3 — Subir a aplicação

```bash
./mvnw spring-boot:run
```

A API sobe em <http://localhost:8080>. No Windows, use `mvnw.cmd spring-boot:run`.

### Modo demonstração (sem Oracle)

Para demonstrar a API em uma máquina sem banco configurado — ou quando o Oracle da FIAP está fora do ar — existe o perfil `demo`, com H2 em memória:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Nesse modo o Hibernate cria o schema a partir das próprias entidades, inclusive as quatro sequences. Os dados somem quando a aplicação para. **O banco oficial do projeto continua sendo o Oracle da FIAP.**

### Abrindo na IDE

O repositório passou a ser um projeto Maven: importe-o pelo **`pom.xml`**, e não como projeto Java simples.

- **IntelliJ IDEA:** `File > Open` → selecione o `pom.xml` → *Open as Project*
- **VS Code:** extensão *Extension Pack for Java*, que detecta o `pom.xml` sozinho
- **Eclipse:** `File > Import > Existing Maven Projects`


## Tabela de Endpoints

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


## Exemplos cURL

> O arquivo [`docs/evidencias-requisicoes.md`](docs/evidencias-requisicoes.md) traz **23 requisições reais**, com o comando enviado, o código HTTP e o corpo devolvido. Ele foi gerado pelo script [`docs/exemplos-curl.sh`](docs/exemplos-curl.sh), que pode ser reexecutado a qualquer momento:
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


## Regras de Negócio

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

A última é uma regra nossa: o objetivo do MOTIVA é priorizar roçada onde ela é necessária, então autorizar intervenção em vegetação baixa desperdiçaria equipe e contradiria o próprio relatório de prioridade.

### A divisão entre DTO e Service

O critério que usamos para decidir onde cada validação mora:

- **Bean Validation no DTO** quando dá para decidir olhando **um campo isolado** (`nivelVegetacaoCm >= 0`);
- **Service** quando é preciso **comparar dois campos** (`kmFinal > kmInicial`), **consultar o banco** (nome duplicado) ou **saber o tipo** do objeto (`idSensor` obrigatório só para monitorado).


## Derived Queries

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

`findByTipo` merece uma nota. `TIPO_TRECHO` é a coluna discriminadora da herança, e o JPA não a expõe como atributo. Para que a *derived query* funcionasse, declaramos um espelho somente-leitura dela em `TrechoRodovia`:

```java
@Column(name = "TIPO_TRECHO", insertable = false, updatable = false)
private String tipo;
```

O `insertable = false, updatable = false` é essencial: a coluna já é escrita pelo mecanismo de herança, e sem isso o Hibernate acusaria dois mapeamentos disputando a mesma coluna.

Para **ver** o SQL gerado, a aplicação sobe com `spring.jpa.show-sql=true`. Chamar `GET /api/trechos?tipo=SECO` imprime no console:

```sql
select t1_0.ID, t1_0.TIPO_TRECHO, t1_0.EM_ESTACAO_SECA, t1_0.ID_EQUIPE_RESPONSAVEL,
       t1_0.ID_SENSOR, t1_0.INDICE_PLUVIOMETRICO, t1_0.NIVEL_VEGETACAO_CM,
       t1_0.QUILOMETRO_FINAL, t1_0.QUILOMETRO_INICIAL
  from TRECHO_RODOVIA t1_0
 where t1_0.TIPO_TRECHO = ?
```


## Perguntas de Reflexão

### 1. Por que o Repository é uma interface e não uma classe? Quem escreve a implementação e quando?

Porque uma interface descreve **o que** queremos do banco, e isso é tudo o que precisamos escrever. O **como** — abrir conexão, montar o SQL, posicionar parâmetros, percorrer o `ResultSet`, mapear colunas para atributos, fechar tudo — é mecânico e idêntico para qualquer entidade. Era justamente esse código repetitivo que ocupava a maior parte dos nossos DAOs na Sprint 3: os quatro DAOs daquele projeto somavam 633 linhas e eram variações do mesmo esqueleto, com os nomes das tabelas trocados.

Quem escreve a implementação é o **Spring Data JPA**, e ele a escreve **em tempo de execução**, quando a aplicação sobe. Ao encontrar uma interface que estende `JpaRepository`, o Spring gera dinamicamente um objeto que a implementa (um *proxy*) e o registra como bean. Esse proxy delega as operações padrão para o `SimpleJpaRepository` e, para os métodos que nós declaramos, usa o nome do método para montar a consulta.

Não existe `TrechoRodoviaRepositoryImpl` em lugar nenhum deste projeto — e, ainda assim, `trechoRepository.save(trecho)` funciona. É possível comprovar isso imprimindo a classe real do bean injetado: ela não é uma classe nossa, é algo como `jdk.proxy.$Proxy123`.

A vantagem prática é que o código que escrevemos passou a ser só o que é específico do MOTIVA. O `EquipeManutencaoRepository` tem quatro assinaturas de método e nenhum corpo, no lugar das 136 linhas do `EquipeManutencaoDAO`.

### 2. O pattern DAO da Sprint 3 "morreu" na migração ou só mudou de forma?

Só mudou de forma — e, na verdade, nem isso: o padrão continua exatamente onde estava, só que agora quem o implementa não somos nós.

O DAO é um padrão de **projeto**, não uma tecnologia. Ele diz: isole o acesso a dados atrás de uma interface, para que o resto do sistema peça objetos de domínio sem saber se eles vêm de Oracle, de um arquivo ou de memória. Essa ideia continua intacta na Sprint 4. O `RelatorioPrioridadeService` pede `trechoRepository.findAll()` e recebe trechos prontos, sem a menor noção de onde eles estavam — exatamente como o `GeradorRelatorio` da Sprint 3 pedia `trechoDAO.listarTodas()`.

O que morreu foi a **implementação manual** do padrão. Comparando papel a papel:

| Papel do DAO | Sprint 3 | Sprint 4 |
|---|---|---|
| Contrato de acesso a dados | métodos públicos do `TrechoRodoviaDAO` | interface `TrechoRodoviaRepository` |
| Tradução objeto ↔ linha | `mapearLinha()` e `paraDominio()` escritos por nós | anotações `@Entity` / `@Column` lidas pelo Hibernate |
| Reconstrução da subclasse certa | `switch` sobre `TIPO_TRECHO` dentro do DAO | `@DiscriminatorValue` resolvido pelo Hibernate |
| Gerenciamento de conexão | `ConexaoBD` (Singleton) | pool do Spring, configurado em `application.properties` |

**O Repository do Spring Data é um DAO gerado pelo framework em tempo de execução.** Trocamos trabalho manual e repetitivo por configuração declarativa, sem abrir mão da separação de responsabilidades que o padrão garante.

### 3. Por que a validação de `nivelVegetacao >= 0` (Sprint 1) deve ficar no Service e não no Controller?

Porque ela é uma regra do **MOTIVA**, e não um detalhe do **HTTP**.

O Controller é só uma das portas de entrada possíveis do sistema. Hoje é a única, mas o próprio histórico deste projeto mostra o contrário: nas Sprints 1 a 3 a porta era um menu de console. Se amanhã o sistema ganhar um importador de CSV da concessionária, um job agendado que lê sensores ou uma fila de mensagens, todos eles vão chamar o `TrechoRodoviaService` — e todos vão herdar a validação automaticamente. Se ela estivesse no Controller, valeria apenas para quem entrasse pela porta do HTTP, e cada nova porta teria que reimplementá-la. Validação duplicada é validação que uma hora vai divergir.

Há outras duas razões concretas:

- **Testabilidade.** Dá para testar a regra chamando o Service diretamente, sem subir servidor nem simular requisição.
- **Vocabulário.** O Service fala a língua do negócio — ele lança `RegraDeNegocioException("O quilometro final deve ser maior que o inicial")`. Quem traduz isso para `400 Bad Request` é o `@RestControllerAdvice`. Assim a regra não precisa conhecer códigos HTTP, e o HTTP não precisa conhecer a regra.

Uma observação sobre como aplicamos isso na prática: o `nivelVegetacao >= 0` em si está declarado como `@PositiveOrZero` no DTO, porque é uma checagem de **formato** sobre um campo isolado, e o Bean Validation já devolve a mensagem por campo de graça. O que exige conhecimento do domínio ficou no Service: `quilometroFinal > quilometroInicial` (compara dois campos), `idSensor` obrigatório só para trechos monitorados (depende do tipo) e a recusa de remover um trecho com histórico (consulta o banco). O princípio é o mesmo nos dois casos — **nada disso está no Controller**.

### 4. No JDBC puro vocês escreviam SQL. Onde está o SQL do `findByTipo()`? Quem o gerou?

O SQL **não existe** enquanto a aplicação está parada. Ele não está no nosso código, não está em arquivo de configuração e não está em nenhum `@Query`. Ele passa a existir quando a aplicação sobe, e quem o escreve é o **Spring Data JPA junto com o Hibernate**.

O processo tem três etapas:

1. **Na subida da aplicação**, o Spring Data encontra `findByTipo` em `TrechoRodoviaRepository`. Remove o prefixo `findBy` e quebra o resto em partes: `Tipo`. Procura então um atributo chamado `tipo` na entidade `TrechoRodovia` — que é o nosso espelho da coluna discriminadora. Como não há sufixo de operador (`GreaterThanEqual`, `Containing`, `IsNull`…), assume igualdade.

2. Com isso ele monta uma consulta **JPQL**, que fala de objetos e não de tabelas: algo equivalente a `select t from TrechoRodovia t where t.tipo = :tipo`. Nesse ponto ainda não há SQL.

3. **Na primeira execução**, o Hibernate traduz essa JPQL para o SQL do banco configurado, usando o dialeto (`OracleDialect`) e o mapeamento das anotações para saber que `TrechoRodovia` é a tabela `TRECHO_RODOVIA` e que `tipo` é a coluna `TIPO_TRECHO`.

Dá para ver o resultado: a aplicação sobe com `spring.jpa.show-sql=true`, e o console imprime o SQL de cada consulta — o `select ... where t1_0.TIPO_TRECHO = ?` mostrado na seção *Derived Queries*.

Vale notar o que ganhamos nessa troca. O `?` daquele SQL é um parâmetro vinculado, não concatenação de texto — ou seja, consultas geradas assim são imunes a SQL injection por construção, enquanto no JDBC puro isso dependia da nossa disciplina de sempre usar `PreparedStatement`. E, porque a tradução para SQL acontece pelo dialeto, a mesma `findByTipo` roda contra o Oracle da FIAP em produção e contra o H2 em memória na suíte de testes, sem uma linha de diferença no código.


## Testes

Nas sprints anteriores os testes eram chamadas manuais dentro do `main.Main`. Agora são automatizados:

```bash
./mvnw test
```

```
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

A classe `MotivaApiIntegrationTest` usa `@SpringBootTest` + `MockMvc` e sobe o contexto Spring inteiro contra um H2 em memória. Além de testar as rotas, isso **valida o mapeamento JPA**: se um `@Column` apontasse para uma coluna inexistente ou um `@SequenceGenerator` estivesse mal declarado, o contexto nem subiria.

- Teste 1 a 4 — CRUD completo de equipe, conferindo `201 → 200 → 200 → 204 → 404`
- Teste 5 — Nome vazio e zero integrantes são rejeitados com `400` e mapa de campos
- Teste 6 a 8 — CRUD completo de trecho, incluindo o cabeçalho `Location` nas respostas `201`
- Teste 9 — Polimorfismo da taxa de crescimento das três subclasses (1,2 / 0,72 / 6,3 cm/dia)
- Teste 10 — Campos exclusivos de subtipo entram e somem do JSON conforme o tipo
- Teste 11 e 12 — As duas *derived queries* do item 3.4
- Teste 13 — Simulação de crescimento aplica a taxa diária da subclasse
- Teste 14 — ID inexistente devolve `404` em `GET`, `PUT` e `DELETE`
- Teste 15 — Roçada mecanizada derruba o nível do trecho para 20 cm e persiste
- Teste 16 — Pulverização registra o produto e **não** altera o nível
- Teste 17 a 19 — Regras de negócio do Service devolvendo `400`
- Teste 20 — Relatório classifica 4 trechos e grava o snapshot no histórico
- Teste 21 e 22 — Consulta por período e validação da janela de datas

Cada teste é `@Transactional`, então tudo o que grava é desfeito ao final — os testes não interferem uns nos outros nem deixam lixo no banco.


## Decisões de Clean Code

- **Injeção por construtor com campos `final`:** na Sprint 3 cada classe criava seus DAOs com `new`; aqui o Spring entrega as dependências prontas, e campos `final` tornam impossível existir um Service pela metade.
- **DTOs separando entidade e contrato:** o cliente não escolhe o próprio id, e renomear uma coluna deixa de quebrar quem consome a API.
- **Exceções de domínio, não códigos HTTP, no Service:** `RecursoNaoEncontradoException` e `RegraDeNegocioException` deixam o Service falando a língua do negócio; o `@RestControllerAdvice` faz a tradução para `404` e `400`.
- **Um único formato de erro:** todas as falhas devolvem o mesmo JSON (`timestamp`, `status`, `erro`, `mensagem`, `caminho`, `campos`), permitindo ao cliente tratá-las genericamente.
- **Stack trace só no log:** erros inesperados devolvem `500` com mensagem genérica — expor detalhes internos na resposta HTTP é falha de segurança.
- **`BooleanSNConverter` centraliza a tradução `boolean` ↔ `CHAR(1)`:** o que estava espalhado por cada `setString`/`getString` do DAO agora existe em um lugar só.
- **`allocationSize = 1` explícito:** o padrão do JPA é 50, o que faria o Hibernate assumir uma sequence com `INCREMENT BY 50` e gerar IDs com buracos em relação às linhas da Sprint 3.
- **`@Enumerated(STRING)` em vez de ordinal:** grava `'HERBICIDA_SELETIVO'` como texto, mantendo compatibilidade com os dados da Sprint 3 e evitando que reordenar o enum corrompa o histórico.
- **`FetchType.LAZY` nas associações:** a equipe só é buscada se alguém chamar `getEquipeResponsavel()`, evitando consultas inúteis ao listar muitos trechos.
- **`open-in-view=false`:** mantém a sessão do JPA restrita à camada de serviço, impedindo consultas preguiçosas de dispararem durante a serialização do JSON.
- **`ddl-auto=none` em produção:** o Hibernate não cria nem altera tabelas — o schema é o que modelamos na Sprint 3 e migramos pelos scripts de `sql/`.
- **Credenciais fora do repositório:** `ORACLE_USER` e `ORACLE_PASSWORD` vêm do ambiente, e o `.gitignore` bloqueia `.env` e `application-local.properties`.
- **Driver via Maven:** o `ojdbc17` é dependência declarada no `pom.xml`; o `lib/ojdbc17.jar` da Sprint 3 deixou de ser necessário para o build.
- **Maven Wrapper versionado:** o projeto roda em qualquer máquina com Java 17, mesmo sem Maven instalado.
- **`.gitattributes` preservando LF:** sem ele, o `mvnw` e os scripts `.sh` quebrariam em Linux e macOS após um clone no Windows.
