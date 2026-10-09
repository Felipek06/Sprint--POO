package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Equipe responsável pela roçada de vegetação nos trechos de rodovia.
 *
 * Esta é a mesma classe da Sprint 1, agora mapeada para a tabela
 * EQUIPE_MANUTENCAO modelada na Sprint 3. O que mudou foi apenas a forma de
 * levar os dados até o banco: o EquipeManutencaoDAO, com seus INSERT,
 * SELECT, UPDATE e DELETE escritos à mão, deu lugar a estas anotações.
 *
 * Duas exigências técnicas do JPA explicam as diferenças em relação ao
 * código da Sprint 1:
 *
 *  - os atributos deixaram de ser final, porque o Hibernate precisa
 *    preencher o objeto depois de criá-lo, ao ler uma linha do banco;
 *  - existe um construtor sem argumentos protegido, que é o único que o
 *    Hibernate sabe chamar para instanciar a entidade.
 */
@Entity
@Table(name = "EQUIPE_MANUTENCAO")
public class EquipeManutencao {

    /**
     * O ID vem da sequence SEQ_EQUIPE_MANUTENCAO, criada pelos scripts da
     * pasta sql/.
     *
     * allocationSize = 1 é obrigatório aqui: o padrão do JPA é 50, o que
     * faria o Hibernate assumir uma sequence com INCREMENT BY 50 e gerar
     * IDs fora de ordem, com buracos, em relação às linhas que a Sprint 3
     * já gravou.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqEquipeManutencao")
    @SequenceGenerator(
            name = "seqEquipeManutencao",
            sequenceName = "SEQ_EQUIPE_MANUTENCAO",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "QUANTIDADE_INTEGRANTES", nullable = false)
    private Integer quantidadeIntegrantes;

    /** Construtor exigido pelo JPA. Não use no código da aplicação. */
    protected EquipeManutencao() {
    }

    public EquipeManutencao(String nome, Integer quantidadeIntegrantes) {
        this.nome = nome;
        this.quantidadeIntegrantes = quantidadeIntegrantes;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getQuantidadeIntegrantes() {
        return quantidadeIntegrantes;
    }

    public void setQuantidadeIntegrantes(Integer quantidadeIntegrantes) {
        this.quantidadeIntegrantes = quantidadeIntegrantes;
    }

    @Override
    public String toString() {
        return String.format("[EquipeManutencao #%s | %s | %s integrante(s)]",
                id, nome, quantidadeIntegrantes);
    }
}
