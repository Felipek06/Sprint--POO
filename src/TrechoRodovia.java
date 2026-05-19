public abstract class TrechoRodovia {

    // Limite a partir do qual o trecho exige intervenção prioritária
    public static final double NIVEL_CRITICO_CM        = 50.0;
    // Limite para roçada mecanizada (vegetação densa)
    public static final double NIVEL_MECANIZADO_CM     = 80.0;

    private final int    quilometroInicial;
    private final int    quilometroFinal;
    private       double nivelVegetacaoCm;
    private EquipeManutencao equipeResponsavel;

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------

    protected TrechoRodovia(int quilometroInicial, int quilometroFinal, double nivelVegetacaoCm) {
        validarQuilometroInicial(quilometroInicial);
        validarQuilometroFinal(quilometroFinal, quilometroInicial);
        validarNivelVegetacao(nivelVegetacaoCm);

        this.quilometroInicial = quilometroInicial;
        this.quilometroFinal   = quilometroFinal;
        this.nivelVegetacaoCm  = nivelVegetacaoCm;
    }

    public abstract double calcularTaxaCrescimentoDiario();

    /**
     * Retorna o nome descritivo do tipo de trecho (ex: "Úmido", "Seco").
     * Usado no relatório de prioridade.
     */
    public abstract String getTipoTrecho();

    // -------------------------------------------------------------------------
    // Comportamentos de domínio
    // -------------------------------------------------------------------------

    /**
     * Simula o crescimento da vegetação ao longo de um número de dias,
     * usando a taxa própria do tipo de trecho.
     *
     * @param dias número de dias a simular (deve ser > 0)
     */
    public void simularCrescimento(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException(
                    "O número de dias deve ser positivo. Valor recebido: " + dias
            );
        }
        double crescimentoTotal = calcularTaxaCrescimentoDiario() * dias;
        nivelVegetacaoCm += crescimentoTotal;
    }

    /**
     * Registra crescimento pontual (medição de campo ou sensor IoT).
     *
     * @param taxaCm crescimento medido em cm (deve ser > 0)
     */
    public void registrarCrescimento(double taxaCm) {
        if (taxaCm <= 0) {
            throw new IllegalArgumentException(
                    "A taxa de crescimento deve ser positiva. Valor recebido: " + taxaCm + " cm"
            );
        }
        nivelVegetacaoCm += taxaCm;
    }

    /**
     * Associa uma equipe de manutenção a este trecho.
     *
     * @param equipe equipe designada (não pode ser nula)
     */
    public void associarEquipe(EquipeManutencao equipe) {
        if (equipe == null) {
            throw new IllegalArgumentException("A equipe de manutenção não pode ser nula.");
        }
        this.equipeResponsavel = equipe;
    }

    /** @return true se a vegetação atingiu ou superou o nível crítico */
    public boolean isCritico() {
        return nivelVegetacaoCm >= NIVEL_CRITICO_CM;
    }

    /** @return true se a densidade exige roçada mecanizada */
    public boolean exigeRocadaMecanizada() {
        return nivelVegetacaoCm >= NIVEL_MECANIZADO_CM;
    }

    // -------------------------------------------------------------------------
    // Validações privadas
    // -------------------------------------------------------------------------

    private void validarQuilometroInicial(int km) {
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
    // Getters
    // -------------------------------------------------------------------------

    public int    getQuilometroInicial()  { return quilometroInicial; }
    public int    getQuilometroFinal()    { return quilometroFinal; }
    public double getNivelVegetacaoCm()   { return nivelVegetacaoCm; }
    public EquipeManutencao getEquipeResponsavel() { return equipeResponsavel; }

    @Override
    public String toString() {
        String status    = isCritico() ? " ⚠ CRÍTICO" : "";
        String equipeInfo = (equipeResponsavel != null)
                ? equipeResponsavel.getNome()
                : "sem equipe";
        return String.format(
                "[%s | KM %d→%d | %.1f cm%s | %s]",
                getTipoTrecho(), quilometroInicial, quilometroFinal,
                nivelVegetacaoCm, status, equipeInfo
        );
    }
}