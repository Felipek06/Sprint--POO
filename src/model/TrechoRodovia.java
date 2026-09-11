package model;

/**
 * Representa um segmento (trecho) de rodovia com controle de vegetação.
 *
 * A partir da Sprint 2 esta classe é abstrata: não faz sentido instanciar
 * um trecho sem um tipo de terreno definido (úmido, seco, monitorado...).
 * Cada subclasse concreta define sua própria taxa de crescimento diário.
 *
 * A partir da Sprint 3, a classe ganha um identificador (id) opcional,
 * atribuído pelo TrechoRodoviaDAO após a persistência no banco Oracle.
 */
public abstract class TrechoRodovia {

    private static final double NIVEL_CRITICO_CM = 50;

    /** Identificador da linha em TRECHO_RODOVIA. Nulo enquanto não persistido. */
    private Long id;

    private final int quilometroInicial;
    private final int quilometroFinal;
    private double nivelVegetacaoCm;
    private EquipeManutencao equipeResponsavel;

    protected TrechoRodovia(int quilometroInicial, int quilometroFinal, double nivelVegetacaoCm) {
        validarQuilometroInicial(quilometroInicial);
        validarQuilometroFinal(quilometroFinal, quilometroInicial);
        validarNivelVegetacao(nivelVegetacaoCm);

        this.quilometroInicial = quilometroInicial;
        this.quilometroFinal = quilometroFinal;
        this.nivelVegetacaoCm = nivelVegetacaoCm;
    }

    // -------------------------------------------------------------------------
    // Contrato abstrato — cada subclasse define seu comportamento
    // -------------------------------------------------------------------------

    /** Taxa de crescimento da vegetação em cm/dia, própria de cada tipo de terreno. */
    public abstract double calcularTaxaCrescimentoDiario();

    /** Nome descritivo do tipo de trecho, usado no relatório de prioridade. */
    public abstract String getTipoTrecho();

    // -------------------------------------------------------------------------
    // Comportamento comum
    // -------------------------------------------------------------------------

    public void registrarCrescimento(double taxaCm) {
        if (taxaCm <= 0) {
            throw new IllegalArgumentException(
                    "A taxa de crescimento deve ser positiva. Valor recebido: " + taxaCm + " cm"
            );
        }
        nivelVegetacaoCm += taxaCm;
    }

    /**
     * Simula o crescimento da vegetação ao longo de um período, usando a taxa
     * diária polimórfica de cada subclasse (Sprint 2 — motor de regras).
     *
     * @param dias número de dias a simular (>= 0)
     */
    public void simularCrescimento(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("O número de dias não pode ser negativo. Valor recebido: " + dias);
        }
        if (dias == 0) {
            return;
        }
        double crescimentoTotal = calcularTaxaCrescimentoDiario() * dias;
        registrarCrescimento(crescimentoTotal);
    }

    public boolean isCritico() {
        return nivelVegetacaoCm >= NIVEL_CRITICO_CM;
    }

    public void associarEquipe(EquipeManutencao equipe) {
        if (equipe == null) {
            throw new IllegalArgumentException("A equipe de manutenção não pode ser nula.");
        }
        this.equipeResponsavel = equipe;
    }

    // -------------------------------------------------------------------------
    // Validações privadas
    // -------------------------------------------------------------------------

    private void validarQuilometroInicial(int km) {
        // Rodovias podem começar no quilômetro 0
        if (km < 0) {
            throw new IllegalArgumentException(
                    "O quilômetro inicial não pode ser negativo. Valor recebido: " + km
            );
        }
    }

    private void validarQuilometroFinal(int kmFinal, int kmInicial) {
        if (kmFinal <= 0) {
            throw new IllegalArgumentException(
                    "O quilômetro final deve ser maior que zero. Valor recebido: " + kmFinal
            );
        }
        if (kmFinal <= kmInicial) {
            throw new IllegalArgumentException(
                    "O quilômetro final (" + kmFinal + ") deve ser maior que o inicial (" + kmInicial + ")."
            );
        }
    }

    private void validarNivelVegetacao(double nivel) {
        if (nivel < 0) {
            throw new IllegalArgumentException(
                    "O nível de vegetação não pode ser negativo. Valor recebido: " + nivel + " cm"
            );
        }
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    /** Usado apenas pela camada de persistência (TrechoRodoviaDAO) para hidratar o id. */
    public void setId(Long id) {
        this.id = id;
    }

    public int getQuilometroInicial() {
        return quilometroInicial;
    }

    public int getQuilometroFinal() {
        return quilometroFinal;
    }

    public double getNivelVegetacaoCm() {
        return nivelVegetacaoCm;
    }

    public EquipeManutencao getEquipeResponsavel() {
        return equipeResponsavel;
    }

    @Override
    public String toString() {
        String statusCritico = isCritico() ? " ESTADO CRÍTICO " : "";
        String equipeInfo = (equipeResponsavel != null)
                ? equipeResponsavel.getNome()
                : "Nenhuma equipe designada";

        return String.format(
                "[%s | KM %d → KM %d | Vegetação: %.1f cm%s | Equipe: %s]",
                getTipoTrecho(), quilometroInicial, quilometroFinal, nivelVegetacaoCm, statusCritico, equipeInfo
        );
    }
}