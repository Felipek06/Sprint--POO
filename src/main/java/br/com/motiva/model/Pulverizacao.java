package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Intervenção de pulverização herbicida, indicada para trechos críticos que
 * ainda não atingiram o nível de roçada mecanizada, ou como tratamento
 * complementar pós-roçada para inibir a rebrota.
 *
 * Equipamento: caminhão tanque com barra de pulverização.
 */
@Entity
@DiscriminatorValue(Pulverizacao.TIPO)
public class Pulverizacao extends IntervencaoOperacional {

    public static final String TIPO = "PULVERIZACAO";

    /** Produtos disponíveis para aplicação. */
    public enum TipoProduto {
        /** Preserva gramíneas e elimina dicotiledôneas. */
        HERBICIDA_SELETIVO,
        /** Elimina toda a vegetação (uso restrito). */
        HERBICIDA_TOTAL,
        /** Retarda o rebroto sem matar a planta. */
        REGULADOR_CRESCIMENTO
    }

    /**
     * @Enumerated(STRING) grava o NOME da constante na coluna, e não o seu
     * índice. É o que mantém a compatibilidade com os dados da Sprint 3, em
     * que TIPO_PRODUTO já guardava 'HERBICIDA_SELETIVO' como texto — e o que
     * evita que reordenar o enum um dia corrompa o histórico.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_PRODUTO", length = 30)
    private TipoProduto tipoProduto;

    protected Pulverizacao() {
    }

    public Pulverizacao(TrechoRodovia trechoAlvo, EquipeManutencao equipeResponsavel,
                        TipoProduto tipoProduto) {
        super(TIPO, trechoAlvo, equipeResponsavel);
        this.tipoProduto = tipoProduto;
    }

    /**
     * A pulverização não derruba a vegetação na hora: o efeito aparece em 7
     * a 14 dias. Por isso, ao contrário da roçada, este método não altera o
     * nível do trecho — apenas registra a aplicação.
     */
    @Override
    public String executarServico() {
        return String.format(
                "Pulverização concluída com caminhão tanque e barra de pulverização. "
                        + "Produto: %s. Nível atual: %.1f cm. Efeito esperado em 7 a 14 dias.",
                tipoProduto == null ? "não informado" : tipoProduto.name().replace('_', ' '),
                getTrechoAlvo().getNivelVegetacaoCm());
    }

    @Override
    public String getDescricaoTipo() {
        return "Pulverização"
                + (tipoProduto == null ? "" : " (" + tipoProduto.name().replace('_', ' ') + ")");
    }

    public TipoProduto getTipoProduto() {
        return tipoProduto;
    }

    public void setTipoProduto(TipoProduto tipoProduto) {
        this.tipoProduto = tipoProduto;
    }
}
