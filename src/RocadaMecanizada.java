/**
 * Intervenção de roçada mecanizada — utilizada em vegetação densa (>= 80 cm).
 *
 * Equipamento: trator com roçadeira lateral ou triturador de galhos.
 * Resultado: reduz o nível da vegetação a um patamar de segurança (20 cm),
 * pois a máquina não consegue rasar completamente o solo.
 */
public class RocadaMecanizada extends IntervencaoOperacional {

    // Nível residual após roçada mecanizada (cm)
    private static final double NIVEL_RESIDUAL_POS_INTERVENCAO_CM = 20.0;

    public RocadaMecanizada(TrechoRodovia trechoAlvo, EquipeManutencao equipeResponsavel) {
        super(trechoAlvo, equipeResponsavel);
    }

    @Override
    public void executarServico() {
        imprimirCabecalhoExecucao();

        double nivelAntes = getTrechoAlvo().getNivelVegetacaoCm();
        System.out.printf(
                "  Nível antes : %.1f cm%n" +
                        "  Equipamento : Trator com roçadeira lateral%n" +
                        "  Nível após  : %.1f cm (residual de segurança)%n",
                nivelAntes, NIVEL_RESIDUAL_POS_INTERVENCAO_CM
        );
        System.out.println("  Status      : ✔ Roçada mecanizada concluída.");
    }

    @Override
    public String getTipoIntervencao() {
        return "Roçada Mecanizada";
    }
}