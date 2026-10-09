package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Serviço executado por uma equipe sobre um trecho de rodovia.
 *
 * Classe abstrata da Sprint 2, agora mapeada para a tabela
 * INTERVENCAO_OPERACIONAL com a mesma estratégia de herança usada nos
 * trechos: uma única tabela, discriminada pela coluna TIPO_INTERVENCAO.
 *
 * ---------------------------------------------------------------------
 * O QUE MUDOU EM executarServico()
 * ---------------------------------------------------------------------
 * Na Sprint 2/3 este método imprimia um bloco formatado no console e não
 * devolvia nada (void). Numa API isso não serve: ninguém do outro lado do
 * HTTP enxerga o System.out do servidor.
 *
 * Agora o método faz duas coisas úteis e observáveis: aplica o efeito real
 * da intervenção sobre o trecho (cada subclasse sabe o seu) e devolve a
 * descrição do que aconteceu, que o Controller entrega como JSON. O
 * polimorfismo da Sprint 2 continua intacto — quem chama não sabe nem
 * precisa saber qual subclasse está executando.
 */
@Entity
@Table(name = "INTERVENCAO_OPERACIONAL")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "TIPO_INTERVENCAO",
        discriminatorType = DiscriminatorType.STRING,
        length = 30
)
public abstract class IntervencaoOperacional {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqIntervencaoOperacional")
    @SequenceGenerator(
            name = "seqIntervencaoOperacional",
            sequenceName = "SEQ_INTERVENCAO_OPERACIONAL",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    /**
     * Espelho somente-leitura do discriminador, para habilitar a derived
     * query findByTipo(String) sem escrever SQL.
     *
     * Assim como em TrechoRodovia, cada subclasse informa o próprio
     * discriminador no construtor, para que um objeto recém-criado responda
     * em getTipo() o mesmo que um objeto lido do banco.
     */
    @Column(name = "TIPO_INTERVENCAO", insertable = false, updatable = false)
    private String tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TRECHO_ALVO", nullable = false)
    private TrechoRodovia trechoAlvo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EQUIPE_RESPONSAVEL", nullable = false)
    private EquipeManutencao equipeResponsavel;

    @Column(name = "DATA_EXECUCAO", nullable = false)
    private LocalDateTime dataExecucao;

    protected IntervencaoOperacional() {
    }

    /**
     * @param tipo valor do discriminador da subclasse, idêntico ao que ela
     *             declara em @DiscriminatorValue
     */
    protected IntervencaoOperacional(String tipo, TrechoRodovia trechoAlvo,
                                     EquipeManutencao equipeResponsavel) {
        this.tipo = tipo;
        this.trechoAlvo = trechoAlvo;
        this.equipeResponsavel = equipeResponsavel;
    }

    /**
     * Carimba a data de execução no momento em que a linha é inserida.
     *
     * Na Sprint 3 quem fazia isso era o DEFAULT SYSTIMESTAMP da coluna, e o
     * objeto Java só descobria a data numa releitura. Com o callback
     * @PrePersist o objeto em memória e a linha no banco já nascem com o
     * mesmo valor.
     */
    @PrePersist
    void aoPersistir() {
        if (dataExecucao == null) {
            dataExecucao = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------------------------
    // Contrato abstrato — cada subclasse define seu comportamento (Sprint 2)
    // -------------------------------------------------------------------------

    /**
     * Executa o serviço sobre o trecho alvo, aplicando o efeito específico
     * do tipo de intervenção.
     *
     * @return descrição do que foi executado, para compor a resposta HTTP
     */
    public abstract String executarServico();

    /** Nome descritivo do tipo de intervenção. */
    public abstract String getDescricaoTipo();

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    /** Valor da coluna discriminadora: ROCADA_MECANIZADA ou PULVERIZACAO. */
    public String getTipo() {
        return tipo;
    }

    public TrechoRodovia getTrechoAlvo() {
        return trechoAlvo;
    }

    public void setTrechoAlvo(TrechoRodovia trechoAlvo) {
        this.trechoAlvo = trechoAlvo;
    }

    public EquipeManutencao getEquipeResponsavel() {
        return equipeResponsavel;
    }

    public void setEquipeResponsavel(EquipeManutencao equipeResponsavel) {
        this.equipeResponsavel = equipeResponsavel;
    }

    public LocalDateTime getDataExecucao() {
        return dataExecucao;
    }

    public void setDataExecucao(LocalDateTime dataExecucao) {
        this.dataExecucao = dataExecucao;
    }
}
