package model;

/**
 * Intervenção de pulverização herbicida — indicada para trechos críticos
 * (>= 50 cm) que não atingiram ainda o nível de roçada mecanizada,
 * ou como tratamento complementar pós-roçada para inibir rebrota.
 *
 * Equipamento: caminhão tanque com barra de pulverização.
 */
public class Pulverizacao extends IntervencaoOperacional {

    // Tipos de produto a aplicar
    public enum TipoProduto {
        HERBICIDA_SELETIVO,   // preserva gramíneas, elimina dicotiledôneas
        HERBICIDA_TOTAL,      // elimina toda vegetação (uso restrito)
        REGULADOR_CRESCIMENTO // retarda o rebroto sem matar a planta
    }

    private final TipoProduto tipoProduto;

    public Pulverizacao(TrechoRodovia trechoAlvo, EquipeManutencao equipeResponsavel,
                        TipoProduto tipoProduto) {
        super(trechoAlvo, equipeResponsavel);

        if (tipoProduto == null) {
            throw new IllegalArgumentException("O tipo de produto não pode ser nulo.");
        }
        this.tipoProduto = tipoProduto;
    }

    @Override
    public void executarServico() {
        imprimirCabecalhoExecucao();

        System.out.printf(
                "  Nível atual : %.1f cm%n" +
                        "  Produto     : %s%n" +
                        "  Equipamento : Caminhão tanque com barra de pulverização%n",
                getTrechoAlvo().getNivelVegetacaoCm(),
                tipoProduto.name().replace("_", " ")
        );
        System.out.println("  Status      : ✔ Pulverização concluída. Efeito em 7-14 dias.");
    }

    @Override
    public String getTipoIntervencao() {
        return "Pulverização (" + tipoProduto.name().replace("_", " ") + ")";
    }

    public TipoProduto getTipoProduto() {
        return tipoProduto;
    }
}