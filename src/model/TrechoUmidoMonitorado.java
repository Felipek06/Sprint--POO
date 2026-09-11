package model;

/**
 * Trecho úmido equipado com sensor IoT de monitoramento vegetal.
 *
 * Demonstra a combinação de herança + interface:
 *  - É um TrechoUmido  →  herda comportamento de crescimento acelerado
 *  - Implementa MonitoravelViaIoT  →  adquire capacidade de transmitir dados
 *
 * O sensor simula uma leitura com pequena variação aleatória (ruído de medição),
 * como acontece em sensores reais de campo.
 */
public class TrechoUmidoMonitorado extends TrechoUmido implements MonitoravelViaIoT {

    // Margem de erro máxima do sensor (± 5%)
    private static final double MARGEM_ERRO_SENSOR = 0.05;

    private final String idSensor;

    /**
     * @param quilometroInicial   km inicial (>= 0)
     * @param quilometroFinal     km final (> quilometroInicial)
     * @param nivelVegetacaoCm    nível inicial em cm (>= 0)
     * @param indicePluviometrico fator de chuva (>= 1.0)
     * @param idSensor            identificador do sensor instalado (não vazio)
     */
    public TrechoUmidoMonitorado(int quilometroInicial, int quilometroFinal,
                                 double nivelVegetacaoCm, double indicePluviometrico,
                                 String idSensor) {
        super(quilometroInicial, quilometroFinal, nivelVegetacaoCm, indicePluviometrico);

        if (idSensor == null || idSensor.isBlank()) {
            throw new IllegalArgumentException("O ID do sensor não pode ser vazio.");
        }
        this.idSensor = idSensor;
    }

    @Override
    public double transmitirDadosSensor() {
        double variacao = 1.0 + (Math.random() * 2 - 1) * MARGEM_ERRO_SENSOR;
        double leitura  = getNivelVegetacaoCm() * variacao;
        System.out.printf(
                "  [IoT] %s → leitura: %.2f cm (nível real: %.2f cm)%n",
                idSensor, leitura, getNivelVegetacaoCm()
        );
        return leitura;
    }

    @Override
    public String getIdSensor() {
        return idSensor;
    }

    @Override
    public String getTipoTrecho() {
        return "Trecho Úmido Monitorado (IoT)";
    }
}