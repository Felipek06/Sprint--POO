package model;

/**
 * Representa uma equipe de manutenção responsável pela roçada
 * de vegetação em trechos de rodovias.
 */
public class EquipeManutencao {

    /** Identificador da linha em EQUIPE_MANUTENCAO. Nulo enquanto não persistido. */
    private Long id;

    private final String nome;
    private final int quantidadeIntegrantes;

    /**
     * @param nome                  nome identificador da equipe (não vazio)
     * @param quantidadeIntegrantes número de integrantes (mínimo 1)
     * @throws IllegalArgumentException se os parâmetros forem inválidos
     */
    public EquipeManutencao(String nome, int quantidadeIntegrantes) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da equipe não pode ser vazio.");
        }
        if (quantidadeIntegrantes < 1) {
            throw new IllegalArgumentException(
                    "A equipe deve ter ao menos 1 integrante. Valor recebido: " + quantidadeIntegrantes
            );
        }
        this.nome = nome;
        this.quantidadeIntegrantes = quantidadeIntegrantes;
    }

    public String getNome()                  { return nome; }
    public int    getQuantidadeIntegrantes() { return quantidadeIntegrantes; }

    public Long getId()          { return id; }
    /** Usado apenas pela camada de persistência (EquipeManutencaoDAO) para hidratar o id. */
    public void setId(Long id)   { this.id = id; }

    @Override
    public String toString() {
        return String.format("[EquipeManutencao | %s | %d integrante(s)]", nome, quantidadeIntegrantes);
    }
}