package model;

/**
 * Trecho de rodovia em região seca (cerrado, caatinga, zonas áridas).
 *
 * Regra de domínio: crescimento mais lento por falta de umidade.
 * Taxa base: 1.2 cm/dia — reduzida ainda mais em períodos de seca.
 */
public class TrechoSeco extends TrechoRodovia {

    private static final double TAXA_BASE_CM_DIA = 1.2;

    // true = estação seca ativa, reduz ainda mais o crescimento
    private final boolean emEstacaoSeca;

    /**
     * @param quilometroInicial km inicial (>= 0)
     * @param quilometroFinal   km final   (> quilometroInicial)
     * @param nivelVegetacaoCm  nível inicial em cm (>= 0)
     * @param emEstacaoSeca     se true, aplica redução extra de 40%
     */
    public TrechoSeco(int quilometroInicial, int quilometroFinal,
                      double nivelVegetacaoCm, boolean emEstacaoSeca) {
        super(quilometroInicial, quilometroFinal, nivelVegetacaoCm);
        this.emEstacaoSeca = emEstacaoSeca;
    }

    /** Construtor simplificado fora da estação seca. */
    public TrechoSeco(int quilometroInicial, int quilometroFinal, double nivelVegetacaoCm) {
        this(quilometroInicial, quilometroFinal, nivelVegetacaoCm, false);
    }

    @Override
    public double calcularTaxaCrescimentoDiario() {
        // Na estação seca, crescimento reduz 40%
        return emEstacaoSeca ? TAXA_BASE_CM_DIA * 0.6 : TAXA_BASE_CM_DIA;
    }

    @Override
    public String getTipoTrecho() {
        return "Trecho Seco";
    }

    public boolean isEmEstacaoSeca() {
        return emEstacaoSeca;
    }
}