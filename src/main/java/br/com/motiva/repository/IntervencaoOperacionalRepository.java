package br.com.motiva.repository;

import br.com.motiva.model.IntervencaoOperacional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório da hierarquia IntervencaoOperacional.
 *
 * Mesma ideia do TrechoRodoviaRepository: o tipo é a classe abstrata, e o
 * Hibernate devolve RocadaMecanizada ou Pulverizacao conforme a coluna
 * TIPO_INTERVENCAO de cada linha.
 */
public interface IntervencaoOperacionalRepository extends JpaRepository<IntervencaoOperacional, Long> {

    /**
     * Derived query sobre o discriminador.
     * Valores aceitos: ROCADA_MECANIZADA e PULVERIZACAO.
     */
    List<IntervencaoOperacional> findByTipo(String tipo);

    /** Histórico de intervenções de um trecho, da mais recente para a mais antiga. */
    List<IntervencaoOperacional> findByTrechoAlvoIdOrderByDataExecucaoDesc(Long idTrecho);

    /** Intervenções executadas por uma equipe. */
    List<IntervencaoOperacional> findByEquipeResponsavelId(Long idEquipe);

    /** Intervenções executadas dentro de um período. */
    List<IntervencaoOperacional> findByDataExecucaoBetweenOrderByDataExecucaoDesc(
            LocalDateTime inicio, LocalDateTime fim);

    /** Usado pelo Service antes de apagar um trecho que ainda tem histórico. */
    boolean existsByTrechoAlvoId(Long idTrecho);

    /** Usado pelo Service antes de apagar uma equipe que ainda tem histórico. */
    boolean existsByEquipeResponsavelId(Long idEquipe);
}
