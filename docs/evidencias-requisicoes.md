# Evidências de requisições — API MOTIVA (Sprint 4)

Gerado em 09/10/2026 11:05:30 contra `http://localhost:8080`.

Cada bloco traz o comando cURL enviado, o código HTTP devolvido e o corpo da resposta.

---

## 1. CRUD de equipes

### Requisição 1 — `POST /api/equipes`

```bash
curl -i -X POST http://localhost:8080/api/equipes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Equipe Beta","quantidadeIntegrantes":4}'
```

Resposta: **HTTP 201**

```json
{
  "id": 2,
  "nome": "Equipe Beta",
  "quantidadeIntegrantes": 4
}
```

### Requisição 2 — `GET /api/equipes`

```bash
curl -i -X GET http://localhost:8080/api/equipes
```

Resposta: **HTTP 200**

```json
[
  {
    "id": 1,
    "nome": "Equipe Alpha",
    "quantidadeIntegrantes": 6
  },
  {
    "id": 2,
    "nome": "Equipe Beta",
    "quantidadeIntegrantes": 4
  }
]
```

### Requisição 3 — `GET /api/equipes/1`

```bash
curl -i -X GET http://localhost:8080/api/equipes/1
```

Resposta: **HTTP 200**

```json
{
  "id": 1,
  "nome": "Equipe Alpha",
  "quantidadeIntegrantes": 6
}
```

---

## 2. CRUD de trechos e herança

### Requisição 4 — `POST /api/trechos`

```bash
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"UMIDO_MONITORADO","quilometroInicial":0,"quilometroFinal":10,"nivelVegetacaoCm":30.0,"indicePluviometrico":1.2,"idSensor":"SENSOR-BR116-KM05"}'
```

Resposta: **HTTP 201**

```json
{
  "id": 4,
  "tipo": "UMIDO_MONITORADO",
  "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
  "quilometroInicial": 0,
  "quilometroFinal": 10,
  "nivelVegetacaoCm": 30.0,
  "taxaCrescimentoDiarioCm": 4.2,
  "critico": false,
  "prioridade": "ATENCAO",
  "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
  "indicePluviometrico": 1.2,
  "idSensor": "SENSOR-BR116-KM05"
}
```

### Requisição 5 — `GET /api/trechos/1`

```bash
curl -i -X GET http://localhost:8080/api/trechos/1
```

Resposta: **HTTP 200**

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

### Requisição 6 — `GET /api/trechos`

```bash
curl -i -X GET http://localhost:8080/api/trechos
```

Resposta: **HTTP 200**

```json
[
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
  },
  {
    "id": 2,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 10,
    "quilometroFinal": 20,
    "nivelVegetacaoCm": 56.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.8
  },
  {
    "id": 3,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 18.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "NORMAL",
    "recomendacao": "Monitoramento de rotina",
    "emEstacaoSeca": false
  },
  {
    "id": 4,
    "tipo": "UMIDO_MONITORADO",
    "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
    "quilometroInicial": 0,
    "quilometroFinal": 10,
    "nivelVegetacaoCm": 30.0,
    "taxaCrescimentoDiarioCm": 4.2,
    "critico": false,
    "prioridade": "ATENCAO",
    "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
    "indicePluviometrico": 1.2,
    "idSensor": "SENSOR-BR116-KM05"
  }
]
```

---

## 3. Derived queries (item 3.4 do enunciado)

Nenhuma linha de SQL foi escrita para estas duas consultas: o Spring Data
gerou o comando a partir do nome dos métodos `findByTipo` e
`findByNivelVegetacaoCmGreaterThanEqual`.

### Requisição 7 — `GET /api/trechos?tipo=SECO`

```bash
curl -i -X GET http://localhost:8080/api/trechos?tipo=SECO
```

Resposta: **HTTP 200**

```json
[
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
  },
  {
    "id": 3,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 18.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "NORMAL",
    "recomendacao": "Monitoramento de rotina",
    "emEstacaoSeca": false
  }
]
```

### Requisição 8 — `GET /api/trechos?nivelMinimoCm=50`

```bash
curl -i -X GET http://localhost:8080/api/trechos?nivelMinimoCm=50
```

Resposta: **HTTP 200**

```json
[
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
  },
  {
    "id": 2,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 10,
    "quilometroFinal": 20,
    "nivelVegetacaoCm": 56.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.8
  }
]
```

---

## 4. Atualização e motor de crescimento (Sprint 2)

### Requisição 9 — `PUT /api/trechos/2`

```bash
curl -i -X PUT http://localhost:8080/api/trechos/2 \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"UMIDO","quilometroInicial":10,"quilometroFinal":20,"nivelVegetacaoCm":60.0,"indicePluviometrico":1.8,"equipeResponsavelId":1}'
```

Resposta: **HTTP 200**

```json
{
  "id": 2,
  "tipo": "UMIDO",
  "descricaoTipo": "Trecho Úmido",
  "quilometroInicial": 10,
  "quilometroFinal": 20,
  "nivelVegetacaoCm": 60.0,
  "taxaCrescimentoDiarioCm": 6.3,
  "critico": true,
  "prioridade": "CRITICO",
  "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
  "indicePluviometrico": 1.8,
  "equipeResponsavel": {
    "id": 1,
    "nome": "Equipe Alpha",
    "quantidadeIntegrantes": 6
  }
}
```

O trecho seco cresce 1,2 cm/dia; em estação seca, 0,72 cm/dia. Dez dias
sobre 18 cm resultam em 25,2 cm — e o trecho sai de NORMAL para ATENÇÃO.

### Requisição 10 — `POST /api/trechos/3/simulacao?dias=10`

```bash
curl -i -X POST http://localhost:8080/api/trechos/3/simulacao?dias=10
```

Resposta: **HTTP 200**

```json
{
  "id": 3,
  "tipo": "SECO",
  "descricaoTipo": "Trecho Seco",
  "quilometroInicial": 40,
  "quilometroFinal": 50,
  "nivelVegetacaoCm": 30.0,
  "taxaCrescimentoDiarioCm": 1.2,
  "critico": false,
  "prioridade": "ATENCAO",
  "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
  "emEstacaoSeca": false
}
```

---

## 5. Intervenções e polimorfismo

O campo `resultadoExecucao` é o retorno de `executarServico()` — o texto
que, nas Sprints 2 e 3, era impresso no console.

### Requisição 11 — `POST /api/intervencoes`

```bash
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"ROCADA_MECANIZADA","trechoAlvoId":1,"equipeResponsavelId":1}'
```

Resposta: **HTTP 201**

```json
{
  "id": 1,
  "tipo": "ROCADA_MECANIZADA",
  "descricaoTipo": "Roçada Mecanizada",
  "dataExecucao": "2026-10-09T11:05:36.2610767",
  "trechoAlvo": {
    "id": 1,
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 30,
    "quilometroFinal": 40,
    "nivelVegetacaoCm": 20.0
  },
  "equipeResponsavel": {
    "id": 1,
    "nome": "Equipe Alpha",
    "quantidadeIntegrantes": 6
  },
  "resultadoExecucao": "Roçada mecanizada concluída com trator e roçadeira lateral. Nível antes: 88,0 cm; nível após: 20,0 cm (residual de segurança)."
}
```

A roçada derrubou o nível do trecho para os 20 cm residuais, e a alteração
foi gravada pelo *dirty checking* do Hibernate, sem nenhum UPDATE manual:

### Requisição 12 — `GET /api/trechos/1`

```bash
curl -i -X GET http://localhost:8080/api/trechos/1
```

Resposta: **HTTP 200**

```json
{
  "id": 1,
  "tipo": "SECO",
  "descricaoTipo": "Trecho Seco",
  "quilometroInicial": 30,
  "quilometroFinal": 40,
  "nivelVegetacaoCm": 20.0,
  "taxaCrescimentoDiarioCm": 0.72,
  "critico": false,
  "prioridade": "NORMAL",
  "recomendacao": "Monitoramento de rotina",
  "emEstacaoSeca": true,
  "equipeResponsavel": {
    "id": 1,
    "nome": "Equipe Alpha",
    "quantidadeIntegrantes": 6
  }
}
```

### Requisição 13 — `POST /api/intervencoes`

```bash
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"PULVERIZACAO","trechoAlvoId":2,"equipeResponsavelId":1,"tipoProduto":"HERBICIDA_SELETIVO"}'
```

Resposta: **HTTP 201**

```json
{
  "id": 2,
  "tipo": "PULVERIZACAO",
  "descricaoTipo": "Pulverização (HERBICIDA SELETIVO)",
  "dataExecucao": "2026-10-09T11:05:37.2479747",
  "tipoProduto": "HERBICIDA_SELETIVO",
  "trechoAlvo": {
    "id": 2,
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 10,
    "quilometroFinal": 20,
    "nivelVegetacaoCm": 60.0
  },
  "equipeResponsavel": {
    "id": 1,
    "nome": "Equipe Alpha",
    "quantidadeIntegrantes": 6
  },
  "resultadoExecucao": "Pulverização concluída com caminhão tanque e barra de pulverização. Produto: HERBICIDA SELETIVO. Nível atual: 60,0 cm. Efeito esperado em 7 a 14 dias."
}
```

### Requisição 14 — `GET /api/intervencoes`

```bash
curl -i -X GET http://localhost:8080/api/intervencoes
```

Resposta: **HTTP 200**

```json
[
  {
    "id": 1,
    "tipo": "ROCADA_MECANIZADA",
    "descricaoTipo": "Roçada Mecanizada",
    "dataExecucao": "2026-10-09T11:05:36.261077",
    "trechoAlvo": {
      "id": 1,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 30,
      "quilometroFinal": 40,
      "nivelVegetacaoCm": 20.0
    },
    "equipeResponsavel": {
      "id": 1,
      "nome": "Equipe Alpha",
      "quantidadeIntegrantes": 6
    }
  },
  {
    "id": 2,
    "tipo": "PULVERIZACAO",
    "descricaoTipo": "Pulverização (HERBICIDA SELETIVO)",
    "dataExecucao": "2026-10-09T11:05:37.247975",
    "tipoProduto": "HERBICIDA_SELETIVO",
    "trechoAlvo": {
      "id": 2,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 10,
      "quilometroFinal": 20,
      "nivelVegetacaoCm": 60.0
    },
    "equipeResponsavel": {
      "id": 1,
      "nome": "Equipe Alpha",
      "quantidadeIntegrantes": 6
    }
  }
]
```

---

## 6. Relatório de prioridade (item 3.3 do enunciado)

### Requisição 15 — `POST /api/relatorios`

```bash
curl -i -X POST http://localhost:8080/api/relatorios
```

Resposta: **HTTP 201**

```json
{
  "relatorio": {
    "id": 1,
    "dataGeracao": "2026-10-09T11:05:38.16784",
    "qtUrgente": 0,
    "qtCritico": 1,
    "qtAtencao": 2,
    "qtNormal": 1,
    "totalTrechosAnalisados": 4,
    "resumo": "4 trecho(s) analisado(s): 0 urgente(s), 1 critico(s), 2 em atencao, 1 normal(is)."
  },
  "itens": [
    {
      "trechoId": 1,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 30,
      "quilometroFinal": 40,
      "nivelVegetacaoCm": 20.0,
      "prioridade": "NORMAL",
      "intervencaoRecomendada": "Monitoramento de rotina"
    },
    {
      "trechoId": 2,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 10,
      "quilometroFinal": 20,
      "nivelVegetacaoCm": 60.0,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 3,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 40,
      "quilometroFinal": 50,
      "nivelVegetacaoCm": 30.0,
      "prioridade": "ATENCAO",
      "intervencaoRecomendada": "Agendar roçada manual nas próximas 2 semanas"
    },
    {
      "trechoId": 4,
      "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
      "quilometroInicial": 0,
      "quilometroFinal": 10,
      "nivelVegetacaoCm": 30.0,
      "leituraSensorCm": 29.26085408191103,
      "prioridade": "ATENCAO",
      "intervencaoRecomendada": "Agendar roçada manual nas próximas 2 semanas"
    }
  ]
}
```

### Requisição 16 — `GET /api/relatorios`

```bash
curl -i -X GET http://localhost:8080/api/relatorios
```

Resposta: **HTTP 200**

```json
[
  {
    "id": 1,
    "dataGeracao": "2026-10-09T11:05:38.16784",
    "qtUrgente": 0,
    "qtCritico": 1,
    "qtAtencao": 2,
    "qtNormal": 1,
    "totalTrechosAnalisados": 4,
    "resumo": "4 trecho(s) analisado(s): 0 urgente(s), 1 critico(s), 2 em atencao, 1 normal(is)."
  }
]
```

### Requisição 17 — `GET /api/relatorios/periodo?inicio=2026-01-01T00:00:00&fim=2026-12-31T23:59:59`

```bash
curl -i -X GET http://localhost:8080/api/relatorios/periodo?inicio=2026-01-01T00:00:00&fim=2026-12-31T23:59:59
```

Resposta: **HTTP 200**

```json
[
  {
    "id": 1,
    "dataGeracao": "2026-10-09T11:05:38.16784",
    "qtUrgente": 0,
    "qtCritico": 1,
    "qtAtencao": 2,
    "qtNormal": 1,
    "totalTrechosAnalisados": 4,
    "resumo": "4 trecho(s) analisado(s): 0 urgente(s), 1 critico(s), 2 em atencao, 1 normal(is)."
  }
]
```

---

## 7. Códigos de erro

### Requisição 18 — `GET /api/trechos/999999`

```bash
curl -i -X GET http://localhost:8080/api/trechos/999999
```

Resposta: **HTTP 404**

```json
{
  "timestamp": "2026-10-09T11:05:39.5392256",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Trecho de id 999999 nao foi encontrado.",
  "caminho": "/api/trechos/999999"
}
```

### Requisição 19 — `POST /api/trechos`

```bash
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"SECO","quilometroInicial":90,"quilometroFinal":100,"nivelVegetacaoCm":-5.0}'
```

Resposta: **HTTP 400**

```json
{
  "timestamp": "2026-10-09T11:05:40.0775763",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Um ou mais campos da requisicao sao invalidos.",
  "caminho": "/api/trechos",
  "campos": {
    "nivelVegetacaoCm": [
      "O nivel de vegetacao nao pode ser negativo."
    ]
  }
}
```

### Requisição 20 — `POST /api/trechos`

```bash
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"SECO","quilometroInicial":100,"quilometroFinal":95,"nivelVegetacaoCm":10.0}'
```

Resposta: **HTTP 400**

```json
{
  "timestamp": "2026-10-09T11:05:40.5945198",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "O quilometro final (95) deve ser maior que o inicial (100).",
  "caminho": "/api/trechos"
}
```

### Requisição 21 — `POST /api/intervencoes`

```bash
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"PULVERIZACAO","trechoAlvoId":2,"equipeResponsavelId":1}'
```

Resposta: **HTTP 400**

```json
{
  "timestamp": "2026-10-09T11:05:41.1366942",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Intervencoes do tipo PULVERIZACAO exigem o campo 'tipoProduto'. Valores aceitos: HERBICIDA_SELETIVO, HERBICIDA_TOTAL, REGULADOR_CRESCIMENTO.",
  "caminho": "/api/intervencoes"
}
```

---

## 8. Remoção

### Requisição 22 — `DELETE /api/trechos/5`

```bash
curl -i -X DELETE http://localhost:8080/api/trechos/5
```

Resposta: **HTTP 204**

```json
(sem corpo)
```

### Requisição 23 — `GET /api/trechos/5`

```bash
curl -i -X GET http://localhost:8080/api/trechos/5
```

Resposta: **HTTP 404**

```json
{
  "timestamp": "2026-10-09T11:05:42.0159145",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Trecho de id 5 nao foi encontrado.",
  "caminho": "/api/trechos/5"
}
```

---

Total: 23 requisições registradas.
