package br.com.motiva.repository;

import br.com.motiva.model.RelatorioPrioridade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório do histórico do motor de prioridade.
 */
public interface RelatorioPrioridadeRepository extends JpaRepository<RelatorioPrioridade, Long> {

    /** Histórico completo, do relatório mais recente para o mais antigo. */
    List<RelatorioPrioridade> findAllByOrderByDataGeracaoDesc();

    /**
     * Derived query que sustenta o endpoint
     * GET /api/relatorios/periodo?inicio=...&fim=...
     *
     * Equivale a:
     *   SELECT * FROM RELATORIO_PRIORIDADE
     *    WHERE DATA_GERACAO BETWEEN ? AND ?
     *    ORDER BY DATA_GERACAO DESC
     *
     * Note que o BETWEEN do Spring Data é inclusivo nas duas pontas, igual
     * ao do SQL.
     */
    List<RelatorioPrioridade> findByDataGeracaoBetweenOrderByDataGeracaoDesc(
            LocalDateTime inicio, LocalDateTime fim);

    /** Relatórios em que havia pelo menos um trecho em estado urgente. */
    List<RelatorioPrioridade> findByQtUrgenteGreaterThanOrderByDataGeracaoDesc(Integer minimo);
}
