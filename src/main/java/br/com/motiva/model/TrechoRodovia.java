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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Segmento (trecho) de rodovia com controle de vegetação.
 *
 * Classe abstrata desde a Sprint 2: não faz sentido um trecho sem tipo de
 * terreno definido. Cada subclasse concreta define sua própria taxa de
 * crescimento diário, e é esse polimorfismo que o motor de prioridade
 * consome.
 *
 * ---------------------------------------------------------------------
 * COMO A HERANÇA DA SPRINT 2 VIROU HERANÇA DE BANCO
 * ---------------------------------------------------------------------
 * Na Sprint 3, o TrechoRodoviaDAO resolvia a herança na unha: lia a coluna
 * TIPO_TRECHO, abria um switch e chamava new TrechoUmido(...),
 * new TrechoSeco(...) ou new TrechoUmidoMonitorado(...) conforme o texto
 * lido.
 *
 * Na Sprint 4 esse switch sumiu. @Inheritance(SINGLE_TABLE) diz ao
 * Hibernate que a hierarquia inteira mora em UMA tabela, e
 * @DiscriminatorColumn aponta qual coluna guarda o tipo. A partir daí é o
 * próprio framework que lê TIPO_TRECHO e instancia a subclasse certa: a
 * mesma lógica, escrita uma vez por quem fez o Hibernate em vez de uma vez
 * por projeto.
 */
@Entity
@Table(name = "TRECHO_RODOVIA")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "TIPO_TRECHO",
        discriminatorType = DiscriminatorType.STRING,
        length = 30
)
public abstract class TrechoRodovia {

    /** Limiar de criticidade herdado da Sprint 1. */
    private static final double NIVEL_CRITICO_CM = 50.0;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqTrechoRodovia")
    @SequenceGenerator(
            name = "seqTrechoRodovia",
            sequenceName = "SEQ_TRECHO_RODOVIA",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    /**
     * Espelho somente-leitura da coluna discriminadora.
     *
     * A coluna TIPO_TRECHO já é escrita pelo próprio mecanismo de herança
     * do JPA, por isso insertable e updatable são false: sem isso o
     * Hibernate reclamaria de dois mapeamentos disputando a mesma coluna.
     *
     * O ganho de ter este campo é que ele habilita a derived query exigida
     * no item 3.4 do enunciado — findByTipo(String) — sem uma linha de SQL.
     *
     * Ao LER do banco, o Hibernate preenche este campo sozinho. Ao CRIAR um
     * objeto novo em memória, porém, ele continuaria nulo até alguém reler a
     * linha, já que a coluna é escrita pelo mecanismo de herança e não por
     * este mapeamento. Por isso cada subclasse informa o próprio
     * discriminador no construtor: assim o objeto recém-criado e o objeto
     * lido do banco respondem a mesma coisa em getTipo().
     */
    @Column(name = "TIPO_TRECHO", insertable = false, updatable = false)
    private String tipo;

    @Column(name = "QUILOMETRO_INICIAL", nullable = false)
    private Integer quilometroInicial;

    @Column(name = "QUILOMETRO_FINAL", nullable = false)
    private Integer quilometroFinal;

    /**
     * Nível de vegetação em centímetros.
     *
     * O @JdbcTypeCode(NUMERIC) é necessário: por padrão o Hibernate mapeia
     * um Double do Java para BINARY_DOUBLE no Oracle, mas a coluna que
     * modelamos na Sprint 3 é NUMBER(7,2). Sem esta anotação o mapeamento
     * fica divergente do banco — algo que só aparece quando se sobe a
     * aplicação com spring.jpa.hibernate.ddl-auto=validate.
     */
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "NIVEL_VEGETACAO_CM", nullable = false, precision = 7, scale = 2)
    private Double nivelVegetacaoCm;

    /**
     * Relacionamento com a equipe responsável.
     *
     * Na Sprint 3 esta associação era um Long solto (idEquipeResponsavel) e
     * o DAO precisava de uma segunda consulta, via EquipeManutencaoDAO, para
     * transformar esse número em objeto. Com @ManyToOne o campo é a própria
     * EquipeManutencao, e o Hibernate faz o join quando o dado for usado.
     *
     * FetchType.LAZY: a equipe só é buscada no banco se alguém realmente
     * chamar getEquipeResponsavel(), o que evita consultas inúteis ao
     * listar muitos trechos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EQUIPE_RESPONSAVEL")
    private EquipeManutencao equipeResponsavel;

    protected TrechoRodovia() {
    }

    /**
     * @param tipo valor do discriminador da subclasse, idêntico ao que ela
     *             declara em @DiscriminatorValue
     */
    protected TrechoRodovia(String tipo, Integer quilometroInicial,
                            Integer quilometroFinal, Double nivelVegetacaoCm) {
        this.tipo = tipo;
        this.quilometroInicial = quilometroInicial;
        this.quilometroFinal = quilometroFinal;
        this.nivelVegetacaoCm = nivelVegetacaoCm;
    }

    // -------------------------------------------------------------------------
    // Contrato abstrato — cada subclasse define seu comportamento (Sprint 2)
    // -------------------------------------------------------------------------

    /** Taxa de crescimento da vegetação em cm/dia, própria de cada terreno. */
    public abstract double calcularTaxaCrescimentoDiario();

    /** Nome descritivo do tipo de trecho, usado no relatório de prioridade. */
    public abstract String getDescricaoTipo();

    // -------------------------------------------------------------------------
    // Comportamento de domínio comum
    // -------------------------------------------------------------------------

    /**
     * Incrementa o nível de vegetação.
     *
     * Esta validação fica na entidade, e não no Service, porque ela não é
     * uma regra sobre a requisição e sim uma invariante do objeto: um
     * crescimento negativo não é "um pedido inválido do usuário", é um
     * estado que esta classe nunca pode alcançar, venha a chamada de onde
     * vier.
     */
    public void registrarCrescimento(double taxaCm) {
        if (taxaCm <= 0) {
            throw new IllegalArgumentException(
                    "A taxa de crescimento deve ser positiva. Valor recebido: " + taxaCm + " cm");
        }
        this.nivelVegetacaoCm += taxaCm;
    }

    /**
     * Simula o crescimento ao longo de um período usando a taxa diária
     * polimórfica de cada subclasse (motor de regras da Sprint 2).
     *
     * @param dias número de dias a simular (maior ou igual a zero)
     */
    public void simularCrescimento(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException(
                    "O numero de dias nao pode ser negativo. Valor recebido: " + dias);
        }
        if (dias == 0) {
            return;
        }
        registrarCrescimento(calcularTaxaCrescimentoDiario() * dias);
    }

    public boolean isCritico() {
        return nivelVegetacaoCm != null && nivelVegetacaoCm >= NIVEL_CRITICO_CM;
    }

    /** Classifica este trecho segundo os limiares da Sprint 2. */
    public Prioridade classificarPrioridade() {
        return Prioridade.classificar(nivelVegetacaoCm == null ? 0.0 : nivelVegetacaoCm);
    }

    public void associarEquipe(EquipeManutencao equipe) {
        this.equipeResponsavel = equipe;
    }

    public void desassociarEquipe() {
        this.equipeResponsavel = null;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    /** Valor da coluna discriminadora: UMIDO, SECO ou UMIDO_MONITORADO. */
    public String getTipo() {
        return tipo;
    }

    public Integer getQuilometroInicial() {
        return quilometroInicial;
    }

    public void setQuilometroInicial(Integer quilometroInicial) {
        this.quilometroInicial = quilometroInicial;
    }

    public Integer getQuilometroFinal() {
        return quilometroFinal;
    }

    public void setQuilometroFinal(Integer quilometroFinal) {
        this.quilometroFinal = quilometroFinal;
    }

    public Double getNivelVegetacaoCm() {
        return nivelVegetacaoCm;
    }

    public void setNivelVegetacaoCm(Double nivelVegetacaoCm) {
        this.nivelVegetacaoCm = nivelVegetacaoCm;
    }

    public EquipeManutencao getEquipeResponsavel() {
        return equipeResponsavel;
    }

    @Override
    public String toString() {
        return String.format("[%s #%s | KM %s -> KM %s | Vegetacao: %.1f cm]",
                getDescricaoTipo(), id, quilometroInicial, quilometroFinal,
                nivelVegetacaoCm == null ? 0.0 : nivelVegetacaoCm);
    }
}
