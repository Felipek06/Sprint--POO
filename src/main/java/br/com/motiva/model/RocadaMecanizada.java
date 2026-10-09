package br.com.motiva.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Intervenção de roçada mecanizada, usada em vegetação densa.
 *
 * Equipamento: trator com roçadeira lateral ou triturador de galhos.
 * Resultado: reduz o nível da vegetação ao patamar de segurança de 20 cm,
 * pois a máquina não consegue rasar completamente o solo.
 */
@Entity
@DiscriminatorValue(RocadaMecanizada.TIPO)
public class RocadaMecanizada extends IntervencaoOperacional {

    public static final String TIPO = "ROCADA_MECANIZADA";

    /** Nível residual após a roçada mecanizada (cm). */
    private static final double NIVEL_RESIDUAL_POS_INTERVENCAO_CM = 20.0;

    protected RocadaMecanizada() {
    }

    public RocadaMecanizada(TrechoRodovia trechoAlvo, EquipeManutencao equipeResponsavel) {
        super(TIPO, trechoAlvo, equipeResponsavel);
    }

    /**
     * Aplica o efeito real da roçada: o trecho passa a ter 20 cm de
     * vegetação.
     *
     * Como o trecho é uma entidade gerenciada pelo JPA dentro de uma
     * transação, essa alteração é gravada no banco automaticamente, sem
     * nenhum UPDATE escrito à mão — é o dirty checking do Hibernate.
     */
    @Override
    public String executarServico() {
        double nivelAntes = getTrechoAlvo().getNivelVegetacaoCm();
        getTrechoAlvo().setNivelVegetacaoCm(NIVEL_RESIDUAL_POS_INTERVENCAO_CM);

        return String.format(
                "Roçada mecanizada concluída com trator e roçadeira lateral. "
                        + "Nível antes: %.1f cm; nível após: %.1f cm (residual de segurança).",
                nivelAntes, NIVEL_RESIDUAL_POS_INTERVENCAO_CM);
    }

    @Override
    public String getDescricaoTipo() {
        return "Roçada Mecanizada";
    }
}
