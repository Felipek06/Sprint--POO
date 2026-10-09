package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Trecho de rodovia em região úmida (margens de rios, baixadas, matas
 * ciliares).
 *
 * Regra de domínio: a vegetação cresce mais rápido devido à umidade do solo.
 * Taxa base de 3,5 cm/dia, ajustada pelo índice pluviométrico.
 *
 * O @DiscriminatorValue é o que amarra esta subclasse ao valor 'UMIDO' da
 * coluna TIPO_TRECHO — exatamente a string que a Sprint 3 já gravava e que
 * a constraint CK_TRECHO_TIPO aceita.
 */
@Entity
@DiscriminatorValue(TrechoUmido.TIPO)
public class TrechoUmido extends TrechoRodovia {

    public static final String TIPO = "UMIDO";

    private static final double TAXA_BASE_CM_DIA = 3.5;

    /**
     * Multiplicador que representa chuvas acima do normal (1.0 = normal).
     *
     * Mesmo motivo do nivelVegetacaoCm em TrechoRodovia: a coluna da
     * Sprint 3 e NUMBER(4,2), e nao o BINARY_DOUBLE que o Hibernate
     * escolheria sozinho para um Double.
     */
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "INDICE_PLUVIOMETRICO", precision = 4, scale = 2)
    private Double indicePluviometrico;

    protected TrechoUmido() {
    }

    public TrechoUmido(Integer quilometroInicial, Integer quilometroFinal,
                       Double nivelVegetacaoCm, Double indicePluviometrico) {
        this(TIPO, quilometroInicial, quilometroFinal, nivelVegetacaoCm, indicePluviometrico);
    }

    /**
     * Construtor usado pela subclasse TrechoUmidoMonitorado, que precisa
     * herdar o comportamento do trecho úmido mas gravar o próprio
     * discriminador.
     */
    protected TrechoUmido(String tipo, Integer quilometroInicial, Integer quilometroFinal,
                          Double nivelVegetacaoCm, Double indicePluviometrico) {
        super(tipo, quilometroInicial, quilometroFinal, nivelVegetacaoCm);
        this.indicePluviometrico = indicePluviometrico;
    }

    @Override
    public double calcularTaxaCrescimentoDiario() {
        double indice = (indicePluviometrico == null) ? 1.0 : indicePluviometrico;
        return TAXA_BASE_CM_DIA * indice;
    }

    @Override
    public String getDescricaoTipo() {
        return "Trecho Úmido";
    }

    public Double getIndicePluviometrico() {
        return indicePluviometrico;
    }

    public void setIndicePluviometrico(Double indicePluviometrico) {
        this.indicePluviometrico = indicePluviometrico;
    }
}
