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







