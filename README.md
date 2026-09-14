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

