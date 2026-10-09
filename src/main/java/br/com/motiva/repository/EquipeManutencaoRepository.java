package br.com.motiva.repository;

import br.com.motiva.model.EquipeManutencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório da entidade EquipeManutencao.
 *
 * Compare com a Sprint 3: o EquipeManutencaoDAO tinha algumas centenas de
 * linhas — cinco constantes de SQL, abertura de Connection, PreparedStatement,
 * ResultSet, mapeamento linha a linha e fechamento de recursos no finally.
 * Tudo isso foi substituído por esta interface vazia.
 *
 * Ao estender JpaRepository a interface já nasce com save, findById,
 * findAll, deleteById, count, existsById e companhia. Não existe uma classe
 * EquipeManutencaoRepositoryImpl em lugar nenhum do projeto: o Spring Data
 * cria a implementação em tempo de execução, por proxy dinâmico, quando a
 * aplicação sobe.
 */
public interface EquipeManutencaoRepository extends JpaRepository<EquipeManutencao, Long> {

    /**
     * Derived query: o Spring lê o NOME do método, quebra em palavras
     * (Nome + IgnoreCase) e monta o SQL sozinho.
     *
     * Equivale a: SELECT * FROM EQUIPE_MANUTENCAO WHERE UPPER(NOME) = UPPER(?)
     */
    Optional<EquipeManutencao> findByNomeIgnoreCase(String nome);

    /**
     * Busca por parte do nome.
     *
     * Equivale a: ... WHERE UPPER(NOME) LIKE UPPER('%' || ? || '%')
     */
    List<EquipeManutencao> findByNomeContainingIgnoreCase(String trecho);

    /**
     * Equipes com efetivo igual ou maior que o informado — útil para
     * descobrir quem tem gente suficiente para uma roçada mecanizada.
     *
     * Equivale a: ... WHERE QUANTIDADE_INTEGRANTES >= ?
     */
    List<EquipeManutencao> findByQuantidadeIntegrantesGreaterThanEqual(Integer minimo);

    /** Usado pelo Service para impedir duas equipes com o mesmo nome. */
    boolean existsByNomeIgnoreCase(String nome);
}
