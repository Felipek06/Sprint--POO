package dao;

import db.ConexaoBD;
import model.EquipeManutencao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) responsável pelo CRUD da tabela EQUIPE_MANUTENCAO.
 *
 * Segue o padrão pedido na Sprint 3: construtor padrão, métodos
 * inserir/buscarPorId/listarTodas/atualizar/deletar, queries SQL como
 * constantes e um record interno (EquipeManutencaoRow) usado para mapear
 * cada linha do ResultSet antes de reconstruir o objeto de domínio
 * EquipeManutencao (que é imutável — nome e quantidadeIntegrantes são final).
 */
public class EquipeManutencaoDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO EQUIPE_MANUTENCAO (NOME, QUANTIDADE_INTEGRANTES) VALUES (?, ?)";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT ID, NOME, QUANTIDADE_INTEGRANTES FROM EQUIPE_MANUTENCAO WHERE ID = ?";

    private static final String SQL_LISTAR_TODAS =
            "SELECT ID, NOME, QUANTIDADE_INTEGRANTES FROM EQUIPE_MANUTENCAO ORDER BY ID";

    private static final String SQL_ATUALIZAR =
            "UPDATE EQUIPE_MANUTENCAO SET NOME = ?, QUANTIDADE_INTEGRANTES = ? WHERE ID = ?";

    private static final String SQL_DELETAR =
            "DELETE FROM EQUIPE_MANUTENCAO WHERE ID = ?";

    /** Representação crua de uma linha da tabela EQUIPE_MANUTENCAO. */
    private record EquipeManutencaoRow(long id, String nome, int quantidadeIntegrantes) {
    }

    public EquipeManutencaoDAO() {
        // construtor padrão exigido pelo enunciado
    }

    /**
     * Insere uma nova equipe no banco e devolve o mesmo objeto com o id
     * gerado pelo Oracle (GENERATED ALWAYS AS IDENTITY) já preenchido.
     */
    public EquipeManutencao inserir(EquipeManutencao equipe) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERIR, new String[]{"ID"})) {
            stmt.setString(1, equipe.getNome());
            stmt.setInt(2, equipe.getQuantidadeIntegrantes());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    equipe.setId(generatedKeys.getLong(1));
                }
            }
        }
        return equipe;
    }

    public EquipeManutencao buscarPorId(Long id) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return paraDominio(mapearLinha(rs));
                }
                return null;
            }
        }
    }

    public List<EquipeManutencao> listarTodas() throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        List<EquipeManutencao> equipes = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_LISTAR_TODAS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                equipes.add(paraDominio(mapearLinha(rs)));
            }
        }
        return equipes;
    }

    /**
     * Atualiza nome e quantidade de integrantes de uma equipe já persistida.
     *
     * @param id                    id da equipe (não pode ser nulo)
     * @param novoNome              novo nome (não vazio)
     * @param novaQuantidade        nova quantidade de integrantes (>= 1)
     * @return true se alguma linha foi atualizada
     */
    public boolean atualizar(Long id, String novoNome, int novaQuantidade) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_ATUALIZAR)) {
            stmt.setString(1, novoNome);
            stmt.setInt(2, novaQuantidade);
            stmt.setLong(3, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETAR)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Mapeamento ResultSet -> record -> objeto de domínio
    // -------------------------------------------------------------------------

    private EquipeManutencaoRow mapearLinha(ResultSet rs) throws SQLException {
        return new EquipeManutencaoRow(
                rs.getLong("ID"),
                rs.getString("NOME"),
                rs.getInt("QUANTIDADE_INTEGRANTES")
        );
    }

    private EquipeManutencao paraDominio(EquipeManutencaoRow row) {
        EquipeManutencao equipe = new EquipeManutencao(row.nome(), row.quantidadeIntegrantes());
        equipe.setId(row.id());
        return equipe;
    }
}