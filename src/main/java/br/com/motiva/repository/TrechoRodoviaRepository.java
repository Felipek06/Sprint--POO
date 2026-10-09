package br.com.motiva.repository;

import br.com.motiva.model.TrechoRodovia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório da hierarquia TrechoRodovia.
 *
 * O tipo declarado é a classe ABSTRATA TrechoRodovia, e não cada subclasse.
 * Isso basta: como a herança é SINGLE_TABLE, o Hibernate lê a coluna
 * TIPO_TRECHO de cada linha e devolve a instância concreta correta
 * (TrechoUmido, TrechoSeco ou TrechoUmidoMonitorado). Ou seja, findAll()
 * devolve uma lista polimórfica pronta para alimentar o motor de prioridade,
 * exatamente como o TrechoRodoviaDAO.listarTodas() fazia na Sprint 3 — só
 * que sem o switch de reconstrução escrito à mão.
 */
public interface TrechoRodoviaRepository extends JpaRepository<TrechoRodovia, Long> {

    /**
     * Derived query exigida no item 3.4 do enunciado.
     *
     * O Spring Data quebra o nome do método em
     * NivelVegetacaoCm + GreaterThanEqual e gera:
     *   SELECT * FROM TRECHO_RODOVIA WHERE NIVEL_VEGETACAO_CM >= ?
     *
     * É o filtro que o relatório usa para listar só os trechos que
     * ultrapassaram um limiar.
     */
    List<TrechoRodovia> findByNivelVegetacaoCmGreaterThanEqual(Double minimo);

    /**
     * Segunda derived query exigida no item 3.4.
     *
     * O campo "tipo" é o espelho somente-leitura da coluna discriminadora
     * TIPO_TRECHO, declarado em TrechoRodovia. Graças a ele o Spring gera:
     *   SELECT * FROM TRECHO_RODOVIA WHERE TIPO_TRECHO = ?
     *
     * Valores aceitos: UMIDO, SECO e UMIDO_MONITORADO.
     */
    List<TrechoRodovia> findByTipo(String tipo);

    /** Trechos ainda sem equipe designada. Equivale a ... WHERE ID_EQUIPE_RESPONSAVEL IS NULL */
    List<TrechoRodovia> findByEquipeResponsavelIsNull();

    /**
     * Trechos de uma equipe específica.
     *
     * Repare que o método navega pela associação: EquipeResponsavel + Id.
     * O Spring Data monta o join sozinho a partir do nome.
     */
    List<TrechoRodovia> findByEquipeResponsavelId(Long idEquipe);

    /** Trechos dentro de uma faixa de quilometragem, ordenados pelo km inicial. */
    List<TrechoRodovia> findByQuilometroInicialGreaterThanEqualAndQuilometroFinalLessThanEqualOrderByQuilometroInicialAsc(
            Integer kmInicial, Integer kmFinal);

    /** Usado pelo Service para impedir dois trechos cobrindo a mesma faixa. */
    boolean existsByQuilometroInicialAndQuilometroFinal(Integer quilometroInicial, Integer quilometroFinal);
}
