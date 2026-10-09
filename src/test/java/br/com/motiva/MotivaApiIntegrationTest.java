package br.com.motiva;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de API com @SpringBootTest + MockMvc (item de bônus do enunciado).
 *
 * A suite sobe o contexto Spring inteiro — entidades, repositórios,
 * serviços e controllers — contra um H2 em memória configurado no perfil
 * "test". Isso significa que, além de testar as rotas, ela também valida o
 * mapeamento JPA: se um @Column apontasse para uma coluna inexistente ou um
 * @SequenceGenerator estivesse mal declarado, o contexto nem subiria.
 *
 * Cada teste é @Transactional, então tudo o que ele grava é desfeito ao
 * final — os testes não interferem uns nos outros nem deixam lixo no banco.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("API MOTIVA - Sprint 4")
class MotivaApiIntegrationTest {

    private final ObjectMapper json = new ObjectMapper();

    private MockMvc mockMvc;

    @Autowired
    MotivaApiIntegrationTest(WebApplicationContext contexto) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    // =========================================================================
    // CRUD de equipes
    // =========================================================================

    @Test
    @DisplayName("CRUD completo de equipe devolve 201, 200, 200, 204 e depois 404")
    void crudCompletoDeEquipe() throws Exception {
        String corpo = """
                { "nome": "Equipe Teste CRUD", "quantidadeIntegrantes": 4 }
                """;

        // POST -> 201 Created, com Location e corpo criado
        String respostaCriacao = mockMvc.perform(post("/api/equipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Equipe Teste CRUD"))
                .andReturn().getResponse().getContentAsString();

        long id = json.readTree(respostaCriacao).get("id").asLong();

        // GET por id -> 200 OK
        mockMvc.perform(get("/api/equipes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeIntegrantes").value(4));

        // PUT -> 200 OK com o corpo atualizado
        mockMvc.perform(put("/api/equipes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Equipe Teste CRUD", "quantidadeIntegrantes": 8 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeIntegrantes").value(8));

        // DELETE -> 204 No Content
        mockMvc.perform(delete("/api/equipes/{id}", id))
                .andExpect(status().isNoContent());

        // GET do que foi apagado -> 404 Not Found
        mockMvc.perform(get("/api/equipes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Equipe sem nome é recusada com 400 e detalha o campo inválido")
    void equipeSemNomeDevolve400() throws Exception {
        mockMvc.perform(post("/api/equipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "   ", "quantidadeIntegrantes": 3 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").isArray());
    }

    @Test
    @DisplayName("Equipe com zero integrantes é recusada com 400")
    void equipeSemIntegrantesDevolve400() throws Exception {
        mockMvc.perform(post("/api/equipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Equipe Vazia", "quantidadeIntegrantes": 0 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.quantidadeIntegrantes").isArray());
    }

    // =========================================================================
    // CRUD de trechos e herança
    // =========================================================================

    @Test
    @DisplayName("CRUD completo de trecho devolve 201, 200, 200, 204 e depois 404")
    void crudCompletoDeTrecho() throws Exception {
        String respostaCriacao = mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "UMIDO",
                                  "quilometroInicial": 100,
                                  "quilometroFinal": 110,
                                  "nivelVegetacaoCm": 46.0,
                                  "indicePluviometrico": 1.8
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tipo").value("UMIDO"))
                .andExpect(jsonPath("$.prioridade").value("ATENCAO"))
                // 3.5 cm/dia * 1.8 de indice pluviometrico = 6.3 cm/dia
                .andExpect(jsonPath("$.taxaCrescimentoDiarioCm").value(6.3))
                .andReturn().getResponse().getContentAsString();

        long id = json.readTree(respostaCriacao).get("id").asLong();

        mockMvc.perform(get("/api/trechos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelVegetacaoCm").value(46.0));

        // Sobe o nivel para 85 cm: o trecho passa a ser URGENTE
        mockMvc.perform(put("/api/trechos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "UMIDO",
                                  "quilometroInicial": 100,
                                  "quilometroFinal": 110,
                                  "nivelVegetacaoCm": 85.0,
                                  "indicePluviometrico": 1.8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridade").value("URGENTE"))
                .andExpect(jsonPath("$.critico").value(true));

        mockMvc.perform(delete("/api/trechos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/trechos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cada subclasse de trecho calcula sua própria taxa de crescimento")
    void polimorfismoDaTaxaDeCrescimento() throws Exception {
        // Trecho seco fora da estacao seca: 1.2 cm/dia
        mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SECO",
                                  "quilometroInicial": 200,
                                  "quilometroFinal": 210,
                                  "nivelVegetacaoCm": 30.0,
                                  "emEstacaoSeca": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taxaCrescimentoDiarioCm").value(1.2))
                .andExpect(jsonPath("$.emEstacaoSeca").value(false))
                // campo exclusivo de trecho umido nao aparece no JSON do seco
                .andExpect(jsonPath("$.indicePluviometrico").doesNotExist());

        // Trecho seco EM estacao seca: 1.2 * 0.6 = 0.72 cm/dia
        mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SECO",
                                  "quilometroInicial": 210,
                                  "quilometroFinal": 220,
                                  "nivelVegetacaoCm": 30.0,
                                  "emEstacaoSeca": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taxaCrescimentoDiarioCm").value(0.72));

        // Trecho umido monitorado: herda a taxa do umido e ganha o sensor
        mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "UMIDO_MONITORADO",
                                  "quilometroInicial": 220,
                                  "quilometroFinal": 230,
                                  "nivelVegetacaoCm": 72.5,
                                  "indicePluviometrico": 1.2,
                                  "idSensor": "SENSOR-TESTE-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSensor").value("SENSOR-TESTE-01"))
                .andExpect(jsonPath("$.prioridade").value("CRITICO"));
    }

    @Test
    @DisplayName("Trecho monitorado sem idSensor é recusado pela regra do Service")
    void trechoMonitoradoSemSensorDevolve400() throws Exception {
        mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "UMIDO_MONITORADO",
                                  "quilometroInicial": 300,
                                  "quilometroFinal": 310,
                                  "nivelVegetacaoCm": 40.0,
                                  "indicePluviometrico": 1.2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("idSensor")));
    }

    @Test
    @DisplayName("Nível de vegetação negativo é recusado com 400")
    void nivelNegativoDevolve400() throws Exception {
        mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SECO",
                                  "quilometroInicial": 400,
                                  "quilometroFinal": 410,
                                  "nivelVegetacaoCm": -5.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nivelVegetacaoCm").isArray());
    }

    @Test
    @DisplayName("Quilômetro final menor que o inicial é recusado pela regra do Service")
    void quilometragemInvertidaDevolve400() throws Exception {
        mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SECO",
                                  "quilometroInicial": 500,
                                  "quilometroFinal": 490,
                                  "nivelVegetacaoCm": 10.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("maior que o inicial")));
    }

    @Test
    @DisplayName("Derived query findByTipo filtra a listagem por tipo de trecho")
    void filtroPorTipoUsaDerivedQuery() throws Exception {
        criarTrechoSeco(600, 610, 30.0);
        criarTrechoSeco(610, 620, 35.0);

        mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "UMIDO",
                                  "quilometroInicial": 620,
                                  "quilometroFinal": 630,
                                  "nivelVegetacaoCm": 30.0,
                                  "indicePluviometrico": 1.0
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/trechos").param("tipo", "SECO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo").value(
                        org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("SECO"))));
    }

    @Test
    @DisplayName("Derived query findByNivelVegetacaoCmGreaterThanEqual filtra por nível mínimo")
    void filtroPorNivelMinimoUsaDerivedQuery() throws Exception {
        criarTrechoSeco(700, 710, 10.0);
        criarTrechoSeco(710, 720, 90.0);

        mockMvc.perform(get("/api/trechos").param("nivelMinimoCm", "80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nivelVegetacaoCm").value(
                        org.hamcrest.Matchers.everyItem(
                                org.hamcrest.Matchers.greaterThanOrEqualTo(80.0))));
    }

    @Test
    @DisplayName("Simulação de crescimento aplica a taxa diária da subclasse")
    void simulacaoDeCrescimentoAplicaTaxaDaSubclasse() throws Exception {
        long id = criarTrechoSeco(800, 810, 10.0);

        // 10 dias * 1.2 cm/dia = 12 cm -> 10 + 12 = 22 cm
        mockMvc.perform(post("/api/trechos/{id}/simulacao", id).param("dias", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelVegetacaoCm").value(22.0));
    }

    @Test
    @DisplayName("ID inexistente devolve 404 em GET, PUT e DELETE")
    void idInexistenteDevolve404() throws Exception {
        mockMvc.perform(get("/api/trechos/{id}", 999_999L))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/trechos/{id}", 999_999L))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/trechos/{id}", 999_999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SECO",
                                  "quilometroInicial": 900,
                                  "quilometroFinal": 910,
                                  "nivelVegetacaoCm": 10.0
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Intervenções e polimorfismo de executarServico()
    // =========================================================================

    @Test
    @DisplayName("Roçada mecanizada derruba o nível do trecho para 20 cm")
    void rocadaMecanizadaAplicaEfeitoNoTrecho() throws Exception {
        long idEquipe = criarEquipe("Equipe Rocada", 6);
        long idTrecho = criarTrechoSeco(1000, 1010, 88.0);

        mockMvc.perform(post("/api/intervencoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "ROCADA_MECANIZADA",
                                  "trechoAlvoId": %d,
                                  "equipeResponsavelId": %d
                                }
                                """.formatted(idTrecho, idEquipe)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ROCADA_MECANIZADA"))
                .andExpect(jsonPath("$.resultadoExecucao").value(
                        org.hamcrest.Matchers.containsString("Roçada mecanizada")));

        // O efeito polimorfico foi persistido pelo dirty checking do Hibernate
        mockMvc.perform(get("/api/trechos/{id}", idTrecho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelVegetacaoCm").value(20.0))
                .andExpect(jsonPath("$.prioridade").value("NORMAL"));
    }

    @Test
    @DisplayName("Pulverização registra o produto e não altera o nível do trecho")
    void pulverizacaoNaoAlteraNivelDoTrecho() throws Exception {
        long idEquipe = criarEquipe("Equipe Pulverizacao", 4);
        long idTrecho = criarTrechoSeco(1100, 1110, 60.0);

        mockMvc.perform(post("/api/intervencoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "PULVERIZACAO",
                                  "trechoAlvoId": %d,
                                  "equipeResponsavelId": %d,
                                  "tipoProduto": "HERBICIDA_SELETIVO"
                                }
                                """.formatted(idTrecho, idEquipe)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoProduto").value("HERBICIDA_SELETIVO"));

        mockMvc.perform(get("/api/trechos/{id}", idTrecho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelVegetacaoCm").value(60.0));
    }

    @Test
    @DisplayName("Pulverização sem tipoProduto é recusada pela regra do Service")
    void pulverizacaoSemProdutoDevolve400() throws Exception {
        long idEquipe = criarEquipe("Equipe Sem Produto", 3);
        long idTrecho = criarTrechoSeco(1200, 1210, 60.0);

        mockMvc.perform(post("/api/intervencoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "PULVERIZACAO",
                                  "trechoAlvoId": %d,
                                  "equipeResponsavelId": %d
                                }
                                """.formatted(idTrecho, idEquipe)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("tipoProduto")));
    }

    @Test
    @DisplayName("Intervenção em trecho NORMAL é recusada pela regra do Service")
    void intervencaoEmTrechoNormalDevolve400() throws Exception {
        long idEquipe = criarEquipe("Equipe Trecho Normal", 3);
        long idTrecho = criarTrechoSeco(1300, 1310, 10.0);

        mockMvc.perform(post("/api/intervencoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "ROCADA_MECANIZADA",
                                  "trechoAlvoId": %d,
                                  "equipeResponsavelId": %d
                                }
                                """.formatted(idTrecho, idEquipe)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("NORMAL")));
    }

    @Test
    @DisplayName("Intervenção com trecho inexistente devolve 404")
    void intervencaoComTrechoInexistenteDevolve404() throws Exception {
        long idEquipe = criarEquipe("Equipe Fantasma", 3);

        mockMvc.perform(post("/api/intervencoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "ROCADA_MECANIZADA",
                                  "trechoAlvoId": 999999,
                                  "equipeResponsavelId": %d
                                }
                                """.formatted(idEquipe)))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Motor de prioridade
    // =========================================================================

    @Test
    @DisplayName("POST /api/relatorios classifica os trechos e grava o histórico")
    void gerarRelatorioClassificaEPersiste() throws Exception {
        criarTrechoSeco(2000, 2010, 88.0);   // URGENTE
        criarTrechoSeco(2010, 2020, 60.0);   // CRITICO
        criarTrechoSeco(2020, 2030, 30.0);   // ATENCAO
        criarTrechoSeco(2030, 2040, 10.0);   // NORMAL

        mockMvc.perform(post("/api/relatorios"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.relatorio.id").isNumber())
                .andExpect(jsonPath("$.relatorio.qtUrgente").value(1))
                .andExpect(jsonPath("$.relatorio.qtCritico").value(1))
                .andExpect(jsonPath("$.relatorio.qtAtencao").value(1))
                .andExpect(jsonPath("$.relatorio.qtNormal").value(1))
                .andExpect(jsonPath("$.relatorio.totalTrechosAnalisados").value(4))
                .andExpect(jsonPath("$.itens", org.hamcrest.Matchers.hasSize(4)));

        // O snapshot ficou no historico
        mockMvc.perform(get("/api/relatorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    @DisplayName("Relatório sem trechos cadastrados devolve 400")
    void relatorioSemTrechosDevolve400() throws Exception {
        mockMvc.perform(post("/api/relatorios"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("Nao ha trechos")));
    }

    @Test
    @DisplayName("Consulta por período devolve os relatórios dentro da janela")
    void consultaPorPeriodoFiltraPorData() throws Exception {
        criarTrechoSeco(2100, 2110, 55.0);

        mockMvc.perform(post("/api/relatorios"))
                .andExpect(status().isCreated());

        // Janela ampla: o relatorio recem-gerado esta dentro
        mockMvc.perform(get("/api/relatorios/periodo")
                        .param("inicio", "2000-01-01T00:00:00")
                        .param("fim", "2100-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));

        // Janela no passado: nada dentro
        mockMvc.perform(get("/api/relatorios/periodo")
                        .param("inicio", "2000-01-01T00:00:00")
                        .param("fim", "2000-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("Período com data inicial posterior à final devolve 400")
    void periodoInvertidoDevolve400() throws Exception {
        mockMvc.perform(get("/api/relatorios/periodo")
                        .param("inicio", "2026-12-31T00:00:00")
                        .param("fim", "2026-01-01T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Período sem o parâmetro obrigatório devolve 400")
    void periodoSemParametroDevolve400() throws Exception {
        mockMvc.perform(get("/api/relatorios/periodo")
                        .param("inicio", "2026-01-01T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Apoio
    // =========================================================================

    private long criarEquipe(String nome, int integrantes) throws Exception {
        String resposta = mockMvc.perform(post("/api/equipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "%s", "quantidadeIntegrantes": %d }
                                """.formatted(nome, integrantes)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(resposta).get("id").asLong();
    }

    private long criarTrechoSeco(int kmInicial, int kmFinal, double nivel) throws Exception {
        String resposta = mockMvc.perform(post("/api/trechos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SECO",
                                  "quilometroInicial": %d,
                                  "quilometroFinal": %d,
                                  "nivelVegetacaoCm": %s,
                                  "emEstacaoSeca": false
                                }
                                """.formatted(kmInicial, kmFinal, nivel)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode corpo = json.readTree(resposta);
        return corpo.get("id").asLong();
    }
}
