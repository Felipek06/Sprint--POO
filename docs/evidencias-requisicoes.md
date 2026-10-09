# Evidências de requisições — API MOTIVA (Sprint 4)

Gerado em 09/10/2026 12:11:18 contra `http://localhost:8080` — banco: **Oracle Database 21c XE (localhost:1521/XEPDB1), schema 565676 — tabelas da Sprint 3 migradas para sequences**.

Cada bloco traz o comando cURL enviado, o código HTTP devolvido e o corpo da resposta.

---

## 1. CRUD de equipes

### Requisição 1 — `POST /api/equipes`

```bash
curl -i -X POST http://localhost:8080/api/equipes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Equipe Beta (Sprint 4)","quantidadeIntegrantes":4}'
```

Resposta: **HTTP 201**

```json
{
  "id": 10,
  "nome": "Equipe Beta (Sprint 4)",
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
    "nome": "Equipe Beta Reforçada",
    "quantidadeIntegrantes": 5
  },
  {
    "id": 10,
    "nome": "Equipe Beta (Sprint 4)",
    "quantidadeIntegrantes": 4
  },
  {
    "id": 4,
    "nome": "Equipe Alpha",
    "quantidadeIntegrantes": 6
  },
  {
    "id": 5,
    "nome": "Equipe Beta Reforçada",
    "quantidadeIntegrantes": 5
  },
  {
    "id": 9,
    "nome": "Equipe Alpha (Sprint 4)",
    "quantidadeIntegrantes": 6
  }
]
```

### Requisição 3 — `GET /api/equipes/9`

```bash
curl -i -X GET http://localhost:8080/api/equipes/9
```

Resposta: **HTTP 200**

```json
{
  "id": 9,
  "nome": "Equipe Alpha (Sprint 4)",
  "quantidadeIntegrantes": 6
}
```

---

## 2. CRUD de trechos e herança

### Requisição 4 — `POST /api/trechos`

```bash
curl -i -X POST http://localhost:8080/api/trechos \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"UMIDO_MONITORADO","quilometroInicial":1000,"quilometroFinal":1010,"nivelVegetacaoCm":30.0,"indicePluviometrico":1.2,"idSensor":"SENSOR-BR116-KM05"}'
```

Resposta: **HTTP 201**

```json
{
  "id": 25,
  "tipo": "UMIDO_MONITORADO",
  "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
  "quilometroInicial": 1000,
  "quilometroFinal": 1010,
  "nivelVegetacaoCm": 30.0,
  "taxaCrescimentoDiarioCm": 4.2,
  "critico": false,
  "prioridade": "ATENCAO",
  "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
  "indicePluviometrico": 1.2,
  "idSensor": "SENSOR-BR116-KM05"
}
```

### Requisição 5 — `GET /api/trechos/22`

```bash
curl -i -X GET http://localhost:8080/api/trechos/22
```

Resposta: **HTTP 200**

```json
{
  "id": 22,
  "tipo": "SECO",
  "descricaoTipo": "Trecho Seco",
  "quilometroInicial": 1030,
  "quilometroFinal": 1040,
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
    "tipo": "UMIDO_MONITORADO",
    "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
    "quilometroInicial": 0,
    "quilometroFinal": 10,
    "nivelVegetacaoCm": 74.62,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "idSensor": "SENSOR-BR116-KM05"
  },
  {
    "id": 2,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 10,
    "quilometroFinal": 20,
    "nivelVegetacaoCm": 136.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "URGENTE",
    "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
    "indicePluviometrico": 1.8
  },
  {
    "id": 3,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 20,
    "quilometroFinal": 30,
    "nivelVegetacaoCm": 41.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "ATENCAO",
    "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
    "emEstacaoSeca": false
  },
  {
    "id": 4,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 30,
    "quilometroFinal": 40,
    "nivelVegetacaoCm": 67.5,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "equipeResponsavel": {
      "id": 1,
      "nome": "Equipe Alpha",
      "quantidadeIntegrantes": 6
    }
  },
  {
    "id": 5,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 76.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "emEstacaoSeca": false,
    "equipeResponsavel": {
      "id": 2,
      "nome": "Equipe Beta Reforçada",
      "quantidadeIntegrantes": 5
    }
  },
  {
    "id": 6,
    "tipo": "UMIDO_MONITORADO",
    "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
    "quilometroInicial": 0,
    "quilometroFinal": 10,
    "nivelVegetacaoCm": 73.92,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "idSensor": "SENSOR-BR116-KM05"
  },
  {
    "id": 7,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 10,
    "quilometroFinal": 20,
    "nivelVegetacaoCm": 136.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "URGENTE",
    "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
    "indicePluviometrico": 1.8
  },
  {
    "id": 8,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 20,
    "quilometroFinal": 30,
    "nivelVegetacaoCm": 41.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "ATENCAO",
    "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
    "emEstacaoSeca": false
  },
  {
    "id": 9,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 30,
    "quilometroFinal": 40,
    "nivelVegetacaoCm": 67.5,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "equipeResponsavel": {
      "id": 4,
      "nome": "Equipe Alpha",
      "quantidadeIntegrantes": 6
    }
  },
  {
    "id": 10,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 76.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "emEstacaoSeca": false,
    "equipeResponsavel": {
      "id": 5,
      "nome": "Equipe Beta Reforçada",
      "quantidadeIntegrantes": 5
    }
  },
  {
    "id": 23,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 1010,
    "quilometroFinal": 1020,
    "nivelVegetacaoCm": 56.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.8
  },
  {
    "id": 24,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 1040,
    "quilometroFinal": 1050,
    "nivelVegetacaoCm": 18.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "NORMAL",
    "recomendacao": "Monitoramento de rotina",
    "emEstacaoSeca": false
  },
  {
    "id": 25,
    "tipo": "UMIDO_MONITORADO",
    "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
    "quilometroInicial": 1000,
    "quilometroFinal": 1010,
    "nivelVegetacaoCm": 30.0,
    "taxaCrescimentoDiarioCm": 4.2,
    "critico": false,
    "prioridade": "ATENCAO",
    "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
    "indicePluviometrico": 1.2,
    "idSensor": "SENSOR-BR116-KM05"
  },
  {
    "id": 22,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 1030,
    "quilometroFinal": 1040,
    "nivelVegetacaoCm": 88.0,
    "taxaCrescimentoDiarioCm": 0.72,
    "critico": true,
    "prioridade": "URGENTE",
    "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
    "emEstacaoSeca": true
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
    "id": 3,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 20,
    "quilometroFinal": 30,
    "nivelVegetacaoCm": 41.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "ATENCAO",
    "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
    "emEstacaoSeca": false
  },
  {
    "id": 5,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 76.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "emEstacaoSeca": false,
    "equipeResponsavel": {
      "id": 2,
      "nome": "Equipe Beta Reforçada",
      "quantidadeIntegrantes": 5
    }
  },
  {
    "id": 8,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 20,
    "quilometroFinal": 30,
    "nivelVegetacaoCm": 41.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "ATENCAO",
    "recomendacao": "Agendar roçada manual nas próximas 2 semanas",
    "emEstacaoSeca": false
  },
  {
    "id": 10,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 76.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "emEstacaoSeca": false,
    "equipeResponsavel": {
      "id": 5,
      "nome": "Equipe Beta Reforçada",
      "quantidadeIntegrantes": 5
    }
  },
  {
    "id": 24,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 1040,
    "quilometroFinal": 1050,
    "nivelVegetacaoCm": 18.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": false,
    "prioridade": "NORMAL",
    "recomendacao": "Monitoramento de rotina",
    "emEstacaoSeca": false
  },
  {
    "id": 22,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 1030,
    "quilometroFinal": 1040,
    "nivelVegetacaoCm": 88.0,
    "taxaCrescimentoDiarioCm": 0.72,
    "critico": true,
    "prioridade": "URGENTE",
    "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
    "emEstacaoSeca": true
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
    "tipo": "UMIDO_MONITORADO",
    "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
    "quilometroInicial": 0,
    "quilometroFinal": 10,
    "nivelVegetacaoCm": 74.62,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "idSensor": "SENSOR-BR116-KM05"
  },
  {
    "id": 2,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 10,
    "quilometroFinal": 20,
    "nivelVegetacaoCm": 136.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "URGENTE",
    "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
    "indicePluviometrico": 1.8
  },
  {
    "id": 4,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 30,
    "quilometroFinal": 40,
    "nivelVegetacaoCm": 67.5,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "equipeResponsavel": {
      "id": 1,
      "nome": "Equipe Alpha",
      "quantidadeIntegrantes": 6
    }
  },
  {
    "id": 5,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 76.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "emEstacaoSeca": false,
    "equipeResponsavel": {
      "id": 2,
      "nome": "Equipe Beta Reforçada",
      "quantidadeIntegrantes": 5
    }
  },
  {
    "id": 6,
    "tipo": "UMIDO_MONITORADO",
    "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
    "quilometroInicial": 0,
    "quilometroFinal": 10,
    "nivelVegetacaoCm": 73.92,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "idSensor": "SENSOR-BR116-KM05"
  },
  {
    "id": 7,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 10,
    "quilometroFinal": 20,
    "nivelVegetacaoCm": 136.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "URGENTE",
    "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
    "indicePluviometrico": 1.8
  },
  {
    "id": 9,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 30,
    "quilometroFinal": 40,
    "nivelVegetacaoCm": 67.5,
    "taxaCrescimentoDiarioCm": 3.5,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.0,
    "equipeResponsavel": {
      "id": 4,
      "nome": "Equipe Alpha",
      "quantidadeIntegrantes": 6
    }
  },
  {
    "id": 10,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 40,
    "quilometroFinal": 50,
    "nivelVegetacaoCm": 76.0,
    "taxaCrescimentoDiarioCm": 1.2,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "emEstacaoSeca": false,
    "equipeResponsavel": {
      "id": 5,
      "nome": "Equipe Beta Reforçada",
      "quantidadeIntegrantes": 5
    }
  },
  {
    "id": 23,
    "tipo": "UMIDO",
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 1010,
    "quilometroFinal": 1020,
    "nivelVegetacaoCm": 56.0,
    "taxaCrescimentoDiarioCm": 6.3,
    "critico": true,
    "prioridade": "CRITICO",
    "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
    "indicePluviometrico": 1.8
  },
  {
    "id": 22,
    "tipo": "SECO",
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 1030,
    "quilometroFinal": 1040,
    "nivelVegetacaoCm": 88.0,
    "taxaCrescimentoDiarioCm": 0.72,
    "critico": true,
    "prioridade": "URGENTE",
    "recomendacao": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE",
    "emEstacaoSeca": true
  }
]
```

---

## 4. Atualização e motor de crescimento (Sprint 2)

### Requisição 9 — `PUT /api/trechos/23`

```bash
curl -i -X PUT http://localhost:8080/api/trechos/23 \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"UMIDO","quilometroInicial":1010,"quilometroFinal":1020,"nivelVegetacaoCm":60.0,"indicePluviometrico":1.8,"equipeResponsavelId":9}'
```

Resposta: **HTTP 200**

```json
{
  "id": 23,
  "tipo": "UMIDO",
  "descricaoTipo": "Trecho Úmido",
  "quilometroInicial": 1010,
  "quilometroFinal": 1020,
  "nivelVegetacaoCm": 60.0,
  "taxaCrescimentoDiarioCm": 6.3,
  "critico": true,
  "prioridade": "CRITICO",
  "recomendacao": "Pulverização herbicida + reavaliar em 7 dias",
  "indicePluviometrico": 1.8,
  "equipeResponsavel": {
    "id": 9,
    "nome": "Equipe Alpha (Sprint 4)",
    "quantidadeIntegrantes": 6
  }
}
```

O trecho seco cresce 1,2 cm/dia; em estação seca, 0,72 cm/dia. Dez dias
sobre 18 cm resultam em 25,2 cm — e o trecho sai de NORMAL para ATENÇÃO.

### Requisição 10 — `POST /api/trechos/24/simulacao?dias=10`

```bash
curl -i -X POST http://localhost:8080/api/trechos/24/simulacao?dias=10
```

Resposta: **HTTP 200**

```json
{
  "id": 24,
  "tipo": "SECO",
  "descricaoTipo": "Trecho Seco",
  "quilometroInicial": 1040,
  "quilometroFinal": 1050,
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
  -d '{"tipo":"ROCADA_MECANIZADA","trechoAlvoId":22,"equipeResponsavelId":9}'
```

Resposta: **HTTP 201**

```json
{
  "id": 3,
  "tipo": "ROCADA_MECANIZADA",
  "descricaoTipo": "Roçada Mecanizada",
  "dataExecucao": "2026-10-09T12:11:24.4431966",
  "trechoAlvo": {
    "id": 22,
    "descricaoTipo": "Trecho Seco",
    "quilometroInicial": 1030,
    "quilometroFinal": 1040,
    "nivelVegetacaoCm": 20.0
  },
  "equipeResponsavel": {
    "id": 9,
    "nome": "Equipe Alpha (Sprint 4)",
    "quantidadeIntegrantes": 6
  },
  "resultadoExecucao": "Roçada mecanizada concluída com trator e roçadeira lateral. Nível antes: 88,0 cm; nível após: 20,0 cm (residual de segurança)."
}
```

A roçada derrubou o nível do trecho para os 20 cm residuais, e a alteração
foi gravada pelo *dirty checking* do Hibernate, sem nenhum UPDATE manual:

### Requisição 12 — `GET /api/trechos/22`

```bash
curl -i -X GET http://localhost:8080/api/trechos/22
```

Resposta: **HTTP 200**

```json
{
  "id": 22,
  "tipo": "SECO",
  "descricaoTipo": "Trecho Seco",
  "quilometroInicial": 1030,
  "quilometroFinal": 1040,
  "nivelVegetacaoCm": 20.0,
  "taxaCrescimentoDiarioCm": 0.72,
  "critico": false,
  "prioridade": "NORMAL",
  "recomendacao": "Monitoramento de rotina",
  "emEstacaoSeca": true,
  "equipeResponsavel": {
    "id": 9,
    "nome": "Equipe Alpha (Sprint 4)",
    "quantidadeIntegrantes": 6
  }
}
```

### Requisição 13 — `POST /api/intervencoes`

```bash
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"PULVERIZACAO","trechoAlvoId":23,"equipeResponsavelId":9,"tipoProduto":"HERBICIDA_SELETIVO"}'
```

Resposta: **HTTP 201**

```json
{
  "id": 4,
  "tipo": "PULVERIZACAO",
  "descricaoTipo": "Pulverização (HERBICIDA SELETIVO)",
  "dataExecucao": "2026-10-09T12:11:25.4382911",
  "tipoProduto": "HERBICIDA_SELETIVO",
  "trechoAlvo": {
    "id": 23,
    "descricaoTipo": "Trecho Úmido",
    "quilometroInicial": 1010,
    "quilometroFinal": 1020,
    "nivelVegetacaoCm": 60.0
  },
  "equipeResponsavel": {
    "id": 9,
    "nome": "Equipe Alpha (Sprint 4)",
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
    "dataExecucao": "2026-09-11T13:49:58.774",
    "trechoAlvo": {
      "id": 1,
      "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
      "quilometroInicial": 0,
      "quilometroFinal": 10,
      "nivelVegetacaoCm": 74.62
    },
    "equipeResponsavel": {
      "id": 5,
      "nome": "Equipe Beta Reforçada",
      "quantidadeIntegrantes": 5
    }
  },
  {
    "id": 2,
    "tipo": "PULVERIZACAO",
    "descricaoTipo": "Pulverização (HERBICIDA SELETIVO)",
    "dataExecucao": "2026-09-11T13:49:58.794",
    "tipoProduto": "HERBICIDA_SELETIVO",
    "trechoAlvo": {
      "id": 2,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 10,
      "quilometroFinal": 20,
      "nivelVegetacaoCm": 136.0
    },
    "equipeResponsavel": {
      "id": 4,
      "nome": "Equipe Alpha",
      "quantidadeIntegrantes": 6
    }
  },
  {
    "id": 3,
    "tipo": "ROCADA_MECANIZADA",
    "descricaoTipo": "Roçada Mecanizada",
    "dataExecucao": "2026-10-09T12:11:24.443197",
    "trechoAlvo": {
      "id": 22,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 1030,
      "quilometroFinal": 1040,
      "nivelVegetacaoCm": 20.0
    },
    "equipeResponsavel": {
      "id": 9,
      "nome": "Equipe Alpha (Sprint 4)",
      "quantidadeIntegrantes": 6
    }
  },
  {
    "id": 4,
    "tipo": "PULVERIZACAO",
    "descricaoTipo": "Pulverização (HERBICIDA SELETIVO)",
    "dataExecucao": "2026-10-09T12:11:25.438291",
    "tipoProduto": "HERBICIDA_SELETIVO",
    "trechoAlvo": {
      "id": 23,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 1010,
      "quilometroFinal": 1020,
      "nivelVegetacaoCm": 60.0
    },
    "equipeResponsavel": {
      "id": 9,
      "nome": "Equipe Alpha (Sprint 4)",
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
    "id": 5,
    "dataGeracao": "2026-10-09T12:11:26.348248",
    "qtUrgente": 2,
    "qtCritico": 7,
    "qtAtencao": 4,
    "qtNormal": 1,
    "totalTrechosAnalisados": 14,
    "resumo": "14 trecho(s) analisado(s): 2 urgente(s), 7 critico(s), 4 em atencao, 1 normal(is)."
  },
  "itens": [
    {
      "trechoId": 1,
      "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
      "quilometroInicial": 0,
      "quilometroFinal": 10,
      "nivelVegetacaoCm": 74.62,
      "leituraSensorCm": 74.41211396124399,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 2,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 10,
      "quilometroFinal": 20,
      "nivelVegetacaoCm": 136.0,
      "prioridade": "URGENTE",
      "intervencaoRecomendada": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE"
    },
    {
      "trechoId": 3,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 20,
      "quilometroFinal": 30,
      "nivelVegetacaoCm": 41.0,
      "prioridade": "ATENCAO",
      "intervencaoRecomendada": "Agendar roçada manual nas próximas 2 semanas"
    },
    {
      "trechoId": 4,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 30,
      "quilometroFinal": 40,
      "nivelVegetacaoCm": 67.5,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 5,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 40,
      "quilometroFinal": 50,
      "nivelVegetacaoCm": 76.0,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 6,
      "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
      "quilometroInicial": 0,
      "quilometroFinal": 10,
      "nivelVegetacaoCm": 76.60299089927376,
      "leituraSensorCm": 76.60299089927376,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 7,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 10,
      "quilometroFinal": 20,
      "nivelVegetacaoCm": 136.0,
      "prioridade": "URGENTE",
      "intervencaoRecomendada": "Roçada Mecanizada — despachar equipe IMEDIATAMENTE"
    },
    {
      "trechoId": 8,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 20,
      "quilometroFinal": 30,
      "nivelVegetacaoCm": 41.0,
      "prioridade": "ATENCAO",
      "intervencaoRecomendada": "Agendar roçada manual nas próximas 2 semanas"
    },
    {
      "trechoId": 9,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 30,
      "quilometroFinal": 40,
      "nivelVegetacaoCm": 67.5,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 10,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 40,
      "quilometroFinal": 50,
      "nivelVegetacaoCm": 76.0,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 23,
      "descricaoTipo": "Trecho Úmido",
      "quilometroInicial": 1010,
      "quilometroFinal": 1020,
      "nivelVegetacaoCm": 60.0,
      "prioridade": "CRITICO",
      "intervencaoRecomendada": "Pulverização herbicida + reavaliar em 7 dias"
    },
    {
      "trechoId": 24,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 1040,
      "quilometroFinal": 1050,
      "nivelVegetacaoCm": 30.0,
      "prioridade": "ATENCAO",
      "intervencaoRecomendada": "Agendar roçada manual nas próximas 2 semanas"
    },
    {
      "trechoId": 25,
      "descricaoTipo": "Trecho Úmido Monitorado (IoT)",
      "quilometroInicial": 1000,
      "quilometroFinal": 1010,
      "nivelVegetacaoCm": 30.0,
      "leituraSensorCm": 29.664804793683228,
      "prioridade": "ATENCAO",
      "intervencaoRecomendada": "Agendar roçada manual nas próximas 2 semanas"
    },
    {
      "trechoId": 22,
      "descricaoTipo": "Trecho Seco",
      "quilometroInicial": 1030,
      "quilometroFinal": 1040,
      "nivelVegetacaoCm": 20.0,
      "prioridade": "NORMAL",
      "intervencaoRecomendada": "Monitoramento de rotina"
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
    "id": 5,
    "dataGeracao": "2026-10-09T12:11:26.348248",
    "qtUrgente": 2,
    "qtCritico": 7,
    "qtAtencao": 4,
    "qtNormal": 1,
    "totalTrechosAnalisados": 14,
    "resumo": "14 trecho(s) analisado(s): 2 urgente(s), 7 critico(s), 4 em atencao, 1 normal(is)."
  },
  {
    "id": 1,
    "dataGeracao": "2026-09-11T13:49:58.81",
    "qtUrgente": 2,
    "qtCritico": 6,
    "qtAtencao": 2,
    "qtNormal": 0,
    "totalTrechosAnalisados": 10,
    "resumo": "10 trecho(s) analisado(s): 2 urgente(s), 6 crítico(s), 2 em atenção, 0 normal(is)."
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
    "id": 5,
    "dataGeracao": "2026-10-09T12:11:26.348248",
    "qtUrgente": 2,
    "qtCritico": 7,
    "qtAtencao": 4,
    "qtNormal": 1,
    "totalTrechosAnalisados": 14,
    "resumo": "14 trecho(s) analisado(s): 2 urgente(s), 7 critico(s), 4 em atencao, 1 normal(is)."
  },
  {
    "id": 1,
    "dataGeracao": "2026-09-11T13:49:58.81",
    "qtUrgente": 2,
    "qtCritico": 6,
    "qtAtencao": 2,
    "qtNormal": 0,
    "totalTrechosAnalisados": 10,
    "resumo": "10 trecho(s) analisado(s): 2 urgente(s), 6 crítico(s), 2 em atenção, 0 normal(is)."
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
  "timestamp": "2026-10-09T12:11:27.7126371",
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
  -d '{"tipo":"SECO","quilometroInicial":1090,"quilometroFinal":1100,"nivelVegetacaoCm":-5.0}'
```

Resposta: **HTTP 400**

```json
{
  "timestamp": "2026-10-09T12:11:28.2269427",
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
  -d '{"tipo":"SECO","quilometroInicial":1100,"quilometroFinal":1095,"nivelVegetacaoCm":10.0}'
```

Resposta: **HTTP 400**

```json
{
  "timestamp": "2026-10-09T12:11:28.7378411",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "O quilometro final (1095) deve ser maior que o inicial (1100).",
  "caminho": "/api/trechos"
}
```

### Requisição 21 — `POST /api/intervencoes`

```bash
curl -i -X POST http://localhost:8080/api/intervencoes \
  -H 'Content-Type: application/json' \
  -d '{"tipo":"PULVERIZACAO","trechoAlvoId":23,"equipeResponsavelId":9}'
```

Resposta: **HTTP 400**

```json
{
  "timestamp": "2026-10-09T12:11:29.2670323",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Intervencoes do tipo PULVERIZACAO exigem o campo 'tipoProduto'. Valores aceitos: HERBICIDA_SELETIVO, HERBICIDA_TOTAL, REGULADOR_CRESCIMENTO.",
  "caminho": "/api/intervencoes"
}
```

---

## 8. Remoção

### Requisição 22 — `DELETE /api/trechos/26`

```bash
curl -i -X DELETE http://localhost:8080/api/trechos/26
```

Resposta: **HTTP 204**

```json
(sem corpo)
```

### Requisição 23 — `GET /api/trechos/26`

```bash
curl -i -X GET http://localhost:8080/api/trechos/26
```

Resposta: **HTTP 404**

```json
{
  "timestamp": "2026-10-09T12:11:30.2542727",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Trecho de id 26 nao foi encontrado.",
  "caminho": "/api/trechos/26"
}
```

---

Total: 23 requisições registradas.
