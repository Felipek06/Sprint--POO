/**
 * Protótipo console — Sprint 2: O Motor de Regras
 *
 * Demonstra:
 *  - Diferentes comportamentos de crescimento (TrechoUmido vs TrechoSeco)
 *  - Hierarquia com classe abstrata (IntervencaoOperacional)
 *  - Contrato de interface (MonitoravelViaIoT)
 *  - Geração do Relatório de Prioridade automático
 */
public class Main {

    private static final String SEPARADOR = "─".repeat(65);

    public static void main(String[] args) {
        System.out.println(SEPARADOR);
        System.out.println("  SPRINT 2 — O MOTOR DE REGRAS");
        System.out.println(SEPARADOR);

        testarPolimorfismoDeCrescimento();
        testarClasseAbstrata();
        testarInterfaceIoT();
        testarMockIoT();
        testarRelatorioCompleto();

        System.out.println("\n" + SEPARADOR);
        System.out.println("  FIM DOS TESTES");
        System.out.println(SEPARADOR);
    }

    // =========================================================================
    // TESTE 1 — Polimorfismo: crescimentos diferentes por tipo de trecho
    // =========================================================================

    private static void testarPolimorfismoDeCrescimento() {
        titulo("TESTE 1 — Polimorfismo: crescimento diferente por tipo de trecho");
        try {
            TrechoUmido umido = new TrechoUmido(0, 10, 0.0, 1.5); // chuva 50% acima
            TrechoSeco  seco  = new TrechoSeco(10, 20, 0.0, true); // estação seca

            umido.simularCrescimento(10);
            seco.simularCrescimento(10);

            System.out.printf("Trecho Úmido (pluvio 1.5x) após 10 dias: %.1f cm%n",
                    umido.getNivelVegetacaoCm());
            System.out.printf("Trecho Seco (estação seca) após 10 dias: %.1f cm%n",
                    seco.getNivelVegetacaoCm());

            assert umido.getNivelVegetacaoCm() > seco.getNivelVegetacaoCm()
                    : "Trecho úmido deve crescer mais que o seco";

            sucesso("Crescimento polimórfico validado — úmido > seco.");
        } catch (Exception e) {
            falha(e.getMessage());
        }
    }

    // =========================================================================
    // TESTE 2 — Classe abstrata não pode ser instanciada diretamente
    // =========================================================================

    private static void testarClasseAbstrata() {
        titulo("TESTE 2 — Impossibilidade de instanciar classes abstratas");

        // TrechoRodovia e IntervencaoOperacional são abstratas.
        // O compilador Java já impede new TrechoRodovia(...) e new IntervencaoOperacional(...).
        // Este teste valida isso em tempo de execução via reflexão.
        try {
            Class<?> classeTrecho      = Class.forName("TrechoRodovia");
            Class<?> classeIntervencao = Class.forName("IntervencaoOperacional");

            boolean trechoAbstrato      = java.lang.reflect.Modifier.isAbstract(classeTrecho.getModifiers());
            boolean intervencaoAbstrata = java.lang.reflect.Modifier.isAbstract(classeIntervencao.getModifiers());

            assert trechoAbstrato      : "TrechoRodovia deveria ser abstrata";
            assert intervencaoAbstrata : "IntervencaoOperacional deveria ser abstrata";

            System.out.println("TrechoRodovia é abstrata?       " + trechoAbstrato);
            System.out.println("IntervencaoOperacional abstrata? " + intervencaoAbstrata);
            sucesso("Ambas as classes base são abstratas — instanciação direta impossível.");
        } catch (ClassNotFoundException e) {
            falha("Classe não encontrada: " + e.getMessage());
        }
    }

    // =========================================================================
    // TESTE 3 — Interface IoT: apenas trechos monitorados transmitem dados
    // =========================================================================

    private static void testarInterfaceIoT() {
        titulo("TESTE 3 — Interface MonitoravelViaIoT em TrechoUmidoMonitorado");
        try {
            TrechoUmidoMonitorado monitorado = new TrechoUmidoMonitorado(
                    20, 30, 60.0, 1.2, "SENSOR-BR116-KM25"
            );
            TrechoSeco semSensor = new TrechoSeco(30, 40, 60.0);

            assert monitorado instanceof MonitoravelViaIoT
                    : "TrechoUmidoMonitorado deve implementar MonitoravelViaIoT";
            assert !(semSensor instanceof MonitoravelViaIoT)
                    : "TrechoSeco não deve implementar MonitoravelViaIoT";

            System.out.println("TrechoUmidoMonitorado é MonitoravelViaIoT? " +
                    (monitorado instanceof MonitoravelViaIoT));
            System.out.println("TrechoSeco é MonitoravelViaIoT?            " +
                    (semSensor instanceof MonitoravelViaIoT));
            System.out.print("Transmitindo dados: ");
            monitorado.transmitirDadosSensor();

            sucesso("Contrato IoT verificado corretamente.");
        } catch (Exception e) {
            falha(e.getMessage());
        }
    }

    // =========================================================================
    // TESTE 4 — Mock IoT: objeto anônimo implementando a interface
    // =========================================================================

    private static void testarMockIoT() {
        titulo("TESTE 4 — Mock de MonitoravelViaIoT (simulação de sensor)");
        try {
            // Objeto anônimo que implementa a interface — padrão Mock para testes
            MonitoravelViaIoT sensorMock = new MonitoravelViaIoT() {
                @Override
                public double transmitirDadosSensor() {
                    return 45.0; // valor fixo para teste determinístico
                }
                @Override
                public String getIdSensor() {
                    return "SENSOR-MOCK-001";
                }
            };

            double leitura = sensorMock.transmitirDadosSensor();

            assert leitura == 45.0 : "Leitura do mock deve ser 45.0";
            assert sensorMock.getIdSensor().equals("SENSOR-MOCK-001");

            System.out.println("Sensor: " + sensorMock.getIdSensor());
            System.out.println("Leitura capturada: " + leitura + " cm");
            sucesso("Mock IoT capturou dados corretamente.");
        } catch (Exception e) {
            falha(e.getMessage());
        }
    }

    // =========================================================================
    // TESTE 5 — Relatório de prioridade completo
    // =========================================================================

    private static void testarRelatorioCompleto() {
        titulo("TESTE 5 — Relatório de Prioridade automático");
        try {
            EquipeManutencao equipeAlpha = new EquipeManutencao("Equipe Alpha", 6);
            EquipeManutencao equipeBeta  = new EquipeManutencao("Equipe Beta",  4);

            // Montagem do array de trechos com cenários variados
            TrechoRodovia[] trechos = {
                    criarTrecho(new TrechoUmidoMonitorado(0,  10, 20.0, 1.0, "SENSOR-BR116-KM05"), 15, null),
                    criarTrecho(new TrechoUmido(10, 20, 10.0, 1.8),  20, null),       // chuvas intensas
                    criarTrecho(new TrechoSeco(20,  30, 5.0,  false), 30, null),       // crescimento lento
                    criarTrecho(new TrechoUmido(30, 40, 50.0, 1.0),   5, equipeAlpha), // já crítico
                    criarTrecho(new TrechoSeco(40,  50, 70.0, false),  5, equipeBeta), // urgente
                    criarTrecho(new TrechoUmidoMonitorado(50, 60, 75.0, 2.0, "SENSOR-BR116-KM55"), 3, null),
            };

            GeradorRelatorio gerador = new GeradorRelatorio();
            gerador.gerarRelatorio(trechos);

            // Testa intervenções nas classes concretas
            titulo("TESTE 5b — Executando intervenções recomendadas");
            TrechoRodovia trechoUrgente = trechos[4]; // TrechoSeco KM 40-50
            new RocadaMecanizada(trechoUrgente, equipeBeta).executarServico();

            TrechoRodovia trechoCritico = trechos[3]; // TrechoUmido KM 30-40
            new Pulverizacao(trechoCritico, equipeAlpha,
                    Pulverizacao.TipoProduto.HERBICIDA_SELETIVO).executarServico();

            sucesso("Relatório e intervenções executados com sucesso.");
        } catch (Exception e) {
            falha("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // Utilitário: cria trecho, simula crescimento e associa equipe
    // =========================================================================

    private static TrechoRodovia criarTrecho(TrechoRodovia trecho, int dias,
                                             EquipeManutencao equipe) {
        trecho.simularCrescimento(dias);
        if (equipe != null) trecho.associarEquipe(equipe);
        return trecho;
    }

    // =========================================================================
    // Utilitários de exibição
    // =========================================================================

    private static void titulo(String descricao) {
        System.out.println("\n" + SEPARADOR);
        System.out.println("  " + descricao);
        System.out.println(SEPARADOR);
    }

    private static void sucesso(String mensagem) {
        System.out.println("✔ PASSOU  → " + mensagem);
    }

    private static void falha(String mensagem) {
        System.out.println("✘ FALHOU  → " + mensagem);
    }
}