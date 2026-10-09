#!/usr/bin/env bash
# =====================================================================
# MOTIVA - Sprint 4
# Roteiro de requisicoes cURL que exercita a API de ponta a ponta.
#
# Como usar:
#   1. Suba a API em outro terminal:
#        ./mvnw spring-boot:run                              (Oracle FIAP)
#        ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo   (H2)
#   2. Rode este script:
#        bash docs/exemplos-curl.sh
#
# Para gravar as evidencias da entrega em um arquivo:
#        bash docs/exemplos-curl.sh > docs/evidencias-requisicoes.md
# =====================================================================

set -u
BASE="${BASE:-http://localhost:8080}"

# Banco contra o qual as evidencias estao sendo geradas. Fica registrado no
# cabecalho do arquivo, para que ninguem confunda uma rodada de demonstracao
# com uma rodada contra o banco oficial do projeto. Informe ao rodar:
#   BANCO="Oracle FIAP" bash docs/exemplos-curl.sh > docs/evidencias-requisicoes.md
BANCO="${BANCO:-nao informado}"

# A API responde em UTF-8. No Windows o Python usa a codificacao da regiao
# (cp1252) para ler o stdin, o que embaralharia os acentos ao formatar o
# JSON; esta variavel forca UTF-8 na entrada e na saida.
export PYTHONUTF8=1

# Numerador das requisicoes
N=0

# ---------------------------------------------------------------------
# chamar <METODO> <ROTA> [CORPO_JSON] - executa e imprime metodo, rota,
# status HTTP e corpo da resposta ja formatado.
# ---------------------------------------------------------------------
chamar() {
  local metodo="$1" rota="$2" corpo="${3:-}"
  N=$((N + 1))

  echo ""
  echo "### Requisição ${N} — \`${metodo} ${rota}\`"
  echo ""
  echo '```bash'
  if [ -n "$corpo" ]; then
    echo "curl -i -X ${metodo} ${BASE}${rota} \\"
    echo "  -H 'Content-Type: application/json' \\"
    echo "  -d '$(echo "$corpo" | tr -d '\n' | sed 's/  */ /g')'"
  else
    echo "curl -i -X ${metodo} ${BASE}${rota}"
  fi
  echo '```'
  echo ""

  local resposta status json
  if [ -n "$corpo" ]; then
    resposta=$(curl -s -w $'\n%{http_code}' -X "$metodo" "${BASE}${rota}" \
      -H 'Content-Type: application/json' -d "$corpo")
  else
    resposta=$(curl -s -w $'\n%{http_code}' -X "$metodo" "${BASE}${rota}")
  fi

  status=$(echo "$resposta" | tail -n 1)
  json=$(echo "$resposta" | sed '$d')

  echo "Resposta: **HTTP ${status}**"
  echo ""
  echo '```json'
  if [ -z "$json" ]; then
    echo "(sem corpo)"
  else
    echo "$json" | python -m json.tool --indent 2 --no-ensure-ascii 2>/dev/null || echo "$json"
  fi
  echo '```'
}

# Extrai o campo "id" do corpo de uma resposta
extrair_id() {
  local metodo="$1" rota="$2" corpo="${3:-}"
  if [ -n "$corpo" ]; then
    curl -s -X "$metodo" "${BASE}${rota}" -H 'Content-Type: application/json' -d "$corpo" \
      | python -c "import sys,json; print(json.load(sys.stdin)['id'])"
  else
    curl -s -X "$metodo" "${BASE}${rota}" \
      | python -c "import sys,json; print(json.load(sys.stdin)['id'])"
  fi
}

echo "# Evidências de requisições — API MOTIVA (Sprint 4)"
echo ""
echo "Gerado em $(date '+%d/%m/%Y %H:%M:%S') contra \`${BASE}\` — banco: **${BANCO}**."
echo ""
echo "Cada bloco traz o comando cURL enviado, o código HTTP devolvido e o corpo da resposta."

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 1. CRUD de equipes"

ID_EQUIPE=$(extrair_id POST /api/equipes '{"nome":"Equipe Alpha","quantidadeIntegrantes":6}')

chamar POST /api/equipes '{"nome":"Equipe Beta","quantidadeIntegrantes":4}'
chamar GET /api/equipes
chamar GET "/api/equipes/${ID_EQUIPE}"

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 2. CRUD de trechos e herança"

ID_URGENTE=$(extrair_id POST /api/trechos \
  '{"tipo":"SECO","quilometroInicial":30,"quilometroFinal":40,"nivelVegetacaoCm":88.0,"emEstacaoSeca":true}')

ID_CRITICO=$(extrair_id POST /api/trechos \
  '{"tipo":"UMIDO","quilometroInicial":10,"quilometroFinal":20,"nivelVegetacaoCm":56.0,"indicePluviometrico":1.8}')

ID_NORMAL=$(extrair_id POST /api/trechos \
  '{"tipo":"SECO","quilometroInicial":40,"quilometroFinal":50,"nivelVegetacaoCm":18.0,"emEstacaoSeca":false}')

chamar POST /api/trechos \
  '{"tipo":"UMIDO_MONITORADO","quilometroInicial":0,"quilometroFinal":10,"nivelVegetacaoCm":30.0,"indicePluviometrico":1.2,"idSensor":"SENSOR-BR116-KM05"}'

chamar GET "/api/trechos/${ID_URGENTE}"
chamar GET /api/trechos

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 3. Derived queries (item 3.4 do enunciado)"
echo ""
echo "Nenhuma linha de SQL foi escrita para estas duas consultas: o Spring Data"
echo "gerou o comando a partir do nome dos métodos \`findByTipo\` e"
echo "\`findByNivelVegetacaoCmGreaterThanEqual\`."

chamar GET "/api/trechos?tipo=SECO"
chamar GET "/api/trechos?nivelMinimoCm=50"

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 4. Atualização e motor de crescimento (Sprint 2)"

chamar PUT "/api/trechos/${ID_CRITICO}" \
  '{"tipo":"UMIDO","quilometroInicial":10,"quilometroFinal":20,"nivelVegetacaoCm":60.0,"indicePluviometrico":1.8,"equipeResponsavelId":'"${ID_EQUIPE}"'}'

echo ""
echo "O trecho seco cresce 1,2 cm/dia; em estação seca, 0,72 cm/dia. Dez dias"
echo "sobre 18 cm resultam em 25,2 cm — e o trecho sai de NORMAL para ATENÇÃO."

chamar POST "/api/trechos/${ID_NORMAL}/simulacao?dias=10"

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 5. Intervenções e polimorfismo"
echo ""
echo "O campo \`resultadoExecucao\` é o retorno de \`executarServico()\` — o texto"
echo "que, nas Sprints 2 e 3, era impresso no console."

chamar POST /api/intervencoes \
  '{"tipo":"ROCADA_MECANIZADA","trechoAlvoId":'"${ID_URGENTE}"',"equipeResponsavelId":'"${ID_EQUIPE}"'}'

echo ""
echo "A roçada derrubou o nível do trecho para os 20 cm residuais, e a alteração"
echo "foi gravada pelo *dirty checking* do Hibernate, sem nenhum UPDATE manual:"

chamar GET "/api/trechos/${ID_URGENTE}"

chamar POST /api/intervencoes \
  '{"tipo":"PULVERIZACAO","trechoAlvoId":'"${ID_CRITICO}"',"equipeResponsavelId":'"${ID_EQUIPE}"',"tipoProduto":"HERBICIDA_SELETIVO"}'

chamar GET /api/intervencoes

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 6. Relatório de prioridade (item 3.3 do enunciado)"

chamar POST /api/relatorios
chamar GET /api/relatorios
chamar GET "/api/relatorios/periodo?inicio=2026-01-01T00:00:00&fim=2026-12-31T23:59:59"

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 7. Códigos de erro"

chamar GET /api/trechos/999999

chamar POST /api/trechos \
  '{"tipo":"SECO","quilometroInicial":90,"quilometroFinal":100,"nivelVegetacaoCm":-5.0}'

chamar POST /api/trechos \
  '{"tipo":"SECO","quilometroInicial":100,"quilometroFinal":95,"nivelVegetacaoCm":10.0}'

chamar POST /api/intervencoes \
  '{"tipo":"PULVERIZACAO","trechoAlvoId":'"${ID_CRITICO}"',"equipeResponsavelId":'"${ID_EQUIPE}"'}'

# =====================================================================
echo ""
echo "---"
echo ""
echo "## 8. Remoção"

ID_DESCARTAVEL=$(extrair_id POST /api/trechos \
  '{"tipo":"SECO","quilometroInicial":700,"quilometroFinal":710,"nivelVegetacaoCm":12.0}')

chamar DELETE "/api/trechos/${ID_DESCARTAVEL}"
chamar GET "/api/trechos/${ID_DESCARTAVEL}"

echo ""
echo "---"
echo ""
echo "Total: ${N} requisições registradas."
