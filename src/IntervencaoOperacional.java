public abstract class IntervencaoOperacional {

    private final TrechoRodovia   trechoAlvo;
    private final EquipeManutencao equipeResponsavel;

    // -------------------------------------------------------------------------
    // Construtor protegido — só subclasses podem invocar
    // -------------------------------------------------------------------------

    protected IntervencaoOperacional(TrechoRodovia trechoAlvo, EquipeManutencao equipeResponsavel) {
        if (trechoAlvo == null) {
            throw new IllegalArgumentException("O trecho alvo da intervenção não pode ser nulo.");
        }
        if (equipeResponsavel == null) {
            throw new IllegalArgumentException("A equipe responsável não pode ser nula.");
        }
        this.trechoAlvo        = trechoAlvo;
        this.equipeResponsavel = equipeResponsavel;
    }

    // -------------------------------------------------------------------------
    // Contrato abstrato — cada subclasse define seu comportamento
    // -------------------------------------------------------------------------

    /**
     * Executa o serviço de intervenção no trecho alvo.
     * Cada subclasse implementa a lógica específica do tipo de serviço.
     */
    public abstract void executarServico();

    /**
     * Retorna o nome descritivo do tipo de intervenção.
     * Usado no relatório de prioridade.
     */
    public abstract String getTipoIntervencao();

    // -------------------------------------------------------------------------
    // Comportamento comum a todas as intervenções
    // -------------------------------------------------------------------------

    /**
     * Imprime o cabeçalho padrão de execução de qualquer intervenção.
     * Chamado pelas subclasses antes da lógica específica.
     */
    protected void imprimirCabecalhoExecucao() {
        System.out.printf(
                "%n  ▶ Executando: %s%n  Trecho : %s%n  Equipe : %s%n",
                getTipoIntervencao(),
                trechoAlvo,
                equipeResponsavel.getNome()
        );
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public TrechoRodovia    getTrechoAlvo()        { return trechoAlvo; }
    public EquipeManutencao getEquipeResponsavel() { return equipeResponsavel; }
}