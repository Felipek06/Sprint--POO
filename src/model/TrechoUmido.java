package model;

/**
 * Trecho de rodovia em região úmida (margens de rios, baixadas, matas ciliares).
 *
 * Regra de domínio: vegetação cresce mais rápido devido à umidade do solo.
 * Taxa base: 3.5 cm/dia — pode ser ajustada pelo índice pluviométrico.
 */
public class TrechoUmido extends TrechoRodovia {

    private static final double TAXA_BASE_CM_DIA = 3.5;

    // Multiplicador que representa chuvas acima do normal (1.0 = normal)
    private final double indicePluviometrico;

    /**
     * @param quilometroInicial    km inicial (>= 0)
     * @param quilometroFinal      km final   (> quilometroInicial)
     * @param nivelVegetacaoCm     nível inicial em cm (>= 0)
     * @param indicePluviometrico  fator de chuva (>= 1.0; ex: 1.5 = 50% mais chuva)
     */
    public TrechoUmido(int quilometroInicial, int quilometroFinal,
                       double nivelVegetacaoCm, double indicePluviometrico) {
        super(quilometroInicial, quilometroFinal, nivelVegetacaoCm);

        if (indicePluviometrico < 1.0) {
            throw new IllegalArgumentException(
                    "O índice pluviométrico deve ser >= 1.0. Valor recebido: " + indicePluviometrico
            );
        }
        this.indicePluviometrico = indicePluviometrico;
    }

    /** Construtor simplificado com índice pluviométrico normal (1.0). */
    public TrechoUmido(int quilometroInicial, int quilometroFinal, double nivelVegetacaoCm) {
        this(quilometroInicial, quilometroFinal, nivelVegetacaoCm, 1.0);
    }

    @Override
    public double calcularTaxaCrescimentoDiario() {
        return TAXA_BASE_CM_DIA * indicePluviometrico;
    }

    @Override
    public String getTipoTrecho() {
        return "Trecho Úmido";
    }

    public double getIndicePluviometrico() {
        return indicePluviometrico;
    }
}