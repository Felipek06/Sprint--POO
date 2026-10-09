package br.com.motiva.model;

import br.com.motiva.model.converter.BooleanSNConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Trecho de rodovia em região seca (cerrado, caatinga, zonas áridas).
 *
 * Regra de domínio: crescimento mais lento por falta de umidade. Taxa base
 * de 1,2 cm/dia, com redução extra de 40% durante a estação seca.
 */
@Entity
@DiscriminatorValue(TrechoSeco.TIPO)
public class TrechoSeco extends TrechoRodovia {

    public static final String TIPO = "SECO";

    private static final double TAXA_BASE_CM_DIA = 1.2;
    private static final double FATOR_ESTACAO_SECA = 0.6;

    /**
     * true = estação seca ativa, reduz ainda mais o crescimento.
     *
     * A coluna no Oracle é CHAR(1) com 'S' ou 'N', e não um booleano. O
     * BooleanSNConverter faz essa tradução automaticamente nos dois
     * sentidos, de modo que o resto do código Java só enxerga um boolean.
     */
    @Convert(converter = BooleanSNConverter.class)
    @Column(name = "EM_ESTACAO_SECA", length = 1)
    private Boolean emEstacaoSeca;

    protected TrechoSeco() {
    }

    public TrechoSeco(Integer quilometroInicial, Integer quilometroFinal,
                      Double nivelVegetacaoCm, Boolean emEstacaoSeca) {
        super(TIPO, quilometroInicial, quilometroFinal, nivelVegetacaoCm);
        this.emEstacaoSeca = (emEstacaoSeca != null) && emEstacaoSeca;
    }

    @Override
    public double calcularTaxaCrescimentoDiario() {
        return isEmEstacaoSeca()
                ? TAXA_BASE_CM_DIA * FATOR_ESTACAO_SECA
                : TAXA_BASE_CM_DIA;
    }

    @Override
    public String getDescricaoTipo() {
        return "Trecho Seco";
    }

    public boolean isEmEstacaoSeca() {
        return emEstacaoSeca != null && emEstacaoSeca;
    }

    public Boolean getEmEstacaoSeca() {
        return emEstacaoSeca;
    }

    public void setEmEstacaoSeca(Boolean emEstacaoSeca) {
        this.emEstacaoSeca = emEstacaoSeca;
    }
}
