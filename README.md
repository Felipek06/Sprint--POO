# Sprint-1---POO

Sistema de Monitoramento e Priorização de Roçada de Vegetação em Rodovias

### Objetivo da Sprint

Modelar o trecho da rodovia e as equipes de manutenção, produzindo um protótipo em console que instancia diferentes trechos, registra níveis simulados de crescimento de vegetação e associa uma equipe de manutenção a um trecho crítico.

### Classes produzidas
## TrechoRodovia

Representa um segmento de rodovia com controle de vegetação. Atributos:
•	quilometroInicial — km de início do trecho (>= 0)
•	quilometroFinal — km de fim do trecho (> quilometroInicial)
•	nivelVegetacaoCm — altura atual da vegetação em cm (>= 0)
•	equipeResponsavel — equipe designada ao trecho (opcional)

Comportamentos:
•	registrarCrescimento(double taxaCm) — incrementa o nível de vegetação
•	associarEquipe(EquipeManutencao equipe) — vincula uma equipe ao trecho
•	isCritico() — retorna true se nivelVegetacaoCm >= 50 cm


## EquipeManutencao
Representa uma equipe responsável pela roçada. Atributos:
•	nome — identificador da equipe (não vazio)
•	quantidadeIntegrantes — número de membros (>= 1)

### Perguntas de Reflexão
## 1. Por que TrechoRodovia é uma classe e "BR-116 KM 10 ao 15" é um objeto?
Classe é o molde — ela define quais atributos e comportamentos um trecho de rodovia pode ter: quilômetro inicial, quilômetro final, nível de vegetação, métodos de crescimento, etc. A classe não existe na memória como dado concreto; ela é uma descrição.

Objeto é uma instância concreta desse molde, com valores reais ocupando espaço na memória. Quando escrevemos new TrechoRodovia(10, 15, 5.0), estamos criando o objeto "BR-116 KM 10 ao 15" — um trecho específico, com dados reais, que pode registrar crescimento e ser associado a uma equipe.

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

### Testes Unitários cobertos no Main

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
•	Nomes expressivos: classes com substantivos (TrechoRodovia, EquipeManutencao), métodos com verbos no infinitivo (registrarCrescimento, associarEquipe).
•	Exceções em vez de prints: IllegalArgumentException permite que o chamador decida como tratar o erro.
•	Validações privadas isoladas: cada regra de domínio vive em seu próprio método privado, mantendo o construtor limpo.
•	Sem setters públicos desnecessários: o estado só muda por métodos de domínio com semântica clara.

# Sprint 2: O Motor de Regras

> *Sistema de Monitoramento e Priorização de Roçada de Vegetação em Rodovias*


## Objetivo da Sprint

Criar o motor de inteligência do sistema: diferentes comportamentos de crescimento de vegetação por tipo de terreno, tipos distintos de intervenção operacional e um algoritmo que varre um array de trechos e gera um **Relatório de Prioridade** automático indicando quais KMs precisam de roçada mecanizada, pulverização ou apenas monitoramento.


## Evolução em relação à Sprint 1

- **`TrechoRodovia` tornou-se abstrata** — não faz sentido instanciar um trecho sem tipo de terreno definido.
- Dois novos tipos concretos: `TrechoUmido` (cresce ~3,5 cm/dia) e `TrechoSeco` (cresce ~1,2 cm/dia).
- Novo método `simularCrescimento(int dias)` usa a taxa própria de cada subclasse — polimorfismo em ação.
- Todas as classes da Sprint 1 são retrocompatíveis; `EquipeManutencao` não sofreu alteração.
---

## Arquitetura — Classes e Interfaces

| Arquivo | Tipo | Responsabilidade |
|---|---|---|
| `TrechoRodovia` | Classe Abstrata | Modelo base de todos os trechos |
| `TrechoUmido` | Subclasse concreta | Crescimento acelerado por umidade |
| `TrechoSeco` | Subclasse concreta | Crescimento reduzido, estação seca |
| `TrechoUmidoMonitorado` | Subclasse + Interface | Úmido com sensor IoT instalado |
| `IntervencaoOperacional` | Classe Abstrata | Base de todas as intervenções |
| `RocadaMecanizada` | Subclasse concreta | Intervenção com trator roçadeira |
| `Pulverizacao` | Subclasse concreta | Herbicida / regulador de crescimento |
| `MonitoravelViaIoT` | Interface | Contrato de transmissão de sensores |
| `GeradorRelatorio` | Classe de serviço | Motor do relatório de prioridade |
| `EquipeManutencao` | Classe concreta (S1) | Herdada da Sprint 1, sem alterações |
 
---

## Perguntas de Reflexão

### 1. Por que não faz sentido executar uma "Intervenção Operacional" genérica?

No domínio da Motiva, toda ordem de serviço precisa especificar exatamente o que será executado: equipamento, produto, procedimento e custo variam completamente entre uma roçada mecanizada e uma pulverização herbicida. Uma "intervenção genérica" não carrega nenhuma dessas informações — ela é apenas um conceito, não uma ação real.

**A classe abstrata força esse contrato em tempo de compilação.** Tentar escrever `new IntervencaoOperacional(trecho, equipe)` não compila. O desenvolvedor é obrigado a escolher `RocadaMecanizada` ou `Pulverizacao` — ou criar uma nova subclasse concreta para um serviço ainda não mapeado.

> Analogia do domínio: um gestor de campo não despacha uma equipe para fazer "alguma coisa" no KM 42. Ele emite uma OS de roçada mecanizada ou de pulverização. A abstração no código reflete essa realidade operacional.
 
---

### 2. Diferença arquitetural: herdar classe abstrata vs. implementar interface

**Herança (`extends` classe abstrata)** define *o que o objeto é* — sua identidade e tipo na hierarquia. `TrechoUmido extends TrechoRodovia` significa que um trecho úmido *é um* trecho de rodovia, compartilha todos os seus atributos e comportamentos, e só pode ter um pai (Java não tem herança múltipla).

**Interface (`implements`)** define *o que o objeto sabe fazer* — uma capacidade adicional desacoplada da hierarquia. `TrechoUmidoMonitorado implements MonitoravelViaIoT` significa que esse trecho *sabe transmitir dados de sensor*, mas isso não muda sua identidade como `TrechoRodovia`. Amanhã, um `TrechoSeco` também pode ganhar sensor sem mudar sua hierarquia — basta implementar a mesma interface.

A regra prática para decidir:

| Situação | Usar |
|---|---|
| "X **é um** Y" | `extends` (herança) |
| "X **sabe fazer** Y" | `implements` (interface) |

**Benefício arquitetural chave:** o `GeradorRelatorio` pode chamar `sensor.transmitirDadosSensor()` em qualquer objeto que implemente `MonitoravelViaIoT` — seja trecho úmido, seco, urbano ou um mock de teste — sem conhecer a classe concreta. Isso é o desacoplamento que o Interface Segregation Principle promove.
 
---

## Lógica do Relatório de Prioridade

O `GeradorRelatorio` classifica cada trecho em quatro faixas:

| Nível (cm) | Prioridade | Intervenção recomendada |
|---|---|---|
| >= 80 cm | 🔴 URGENTE | Roçada Mecanizada — despachar equipe imediatamente |
| >= 50 cm | 🟠 CRÍTICO | Pulverização herbicida + reavaliar em 7 dias |
| >= 25 cm | 🟡 ATENÇÃO | Agendar roçada manual nas próximas 2 semanas |
| < 25 cm | 🟢 NORMAL | Monitoramento de rotina |

Para trechos `MonitoravelViaIoT`, o relatório consulta `transmitirDadosSensor()` antes de classificar, atualizando o nível automaticamente sem necessidade de inspeção visual.
 
---

## Testes cobertos no Main

| # | Cenário | O que valida |
|---|---|---|
| 1 | Polimorfismo de crescimento | `TrechoUmido` cresce mais que `TrechoSeco` no mesmo período |
| 2 | Abstrações não instanciáveis | Reflexão confirma que `TrechoRodovia` e `IntervencaoOperacional` são abstratas |
| 3 | Contrato IoT | Apenas `TrechoUmidoMonitorado` implementa `MonitoravelViaIoT` |
| 4 | Mock IoT | Objeto anônimo implementa a interface e retorna leitura determinística |
| 5 | Relatório completo | Array de 6 trechos gera relatório com classificação e resumo executivo |
| 5b | Execução de intervenções | `RocadaMecanizada` e `Pulverizacao` executam sobre trechos urgente e crítico |
 
---

## Decisões de Clean Code

- **Classes abstratas com `protected`:** o construtor de `TrechoRodovia` é `protected` — impede instanciação direta mesmo por reflexão.
- **Interface enxuta (ISP):** `MonitoravelViaIoT` tem apenas 2 métodos. Nenhuma responsabilidade de trecho ou equipe vazou para ela.
- **Enum interno em `Pulverizacao`:** `TipoProduto` torna o tipo de produto explícito e seguro em vez de usar strings livres.
- **Enum privado em `GeradorRelatorio`:** `Prioridade` encapsula a lógica de classificação dentro do gerador, sem expor ao restante do sistema.
- **Pattern matching (`instanceof`):** uso de `trecho instanceof MonitoravelViaIoT sensor` (Java 16+) evita cast explícito e torna o código mais seguro e legível.
- **Método template protegido:** `imprimirCabecalhoExecucao()` em `IntervencaoOperacional` padroniza a saída de todas as subclasses sem duplicar código.
 





