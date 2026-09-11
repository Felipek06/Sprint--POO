package dao;

import db.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo CRUD da tabela RELATORIO_PRIORIDADE — o histórico de
 * execuções do GeradorRelatorio. Como cada linha é apenas um "retrato"
 * (snapshot) de uma execução do relatório, o record RelatorioRegistrado
 * é, ao mesmo tempo, a entidade e o tipo de retorno público do DAO.
 */
public class RelatorioPrioridadeDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO RELATORIO_PRIORIDADE (QT_URGENTE, QT_CRITICO, QT_ATENCAO, QT_NORMAL, RESUMO) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT ID, DATA_GERACAO, QT_URGENTE, QT_CRITICO, QT_ATENCAO, QT_NORMAL, RESUMO " +
                    "FROM RELATORIO_PRIORIDADE WHERE ID = ?";

    private static final String SQL_LISTAR_TODAS =
            "SELECT ID, DATA_GERACAO, QT_URGENTE, QT_CRITICO, QT_ATENCAO, QT_NORMAL, RESUMO " +
                    "FROM RELATORIO_PRIORIDADE ORDER BY ID";

    private static final String SQL_ATUALIZAR =
            "UPDATE RELATORIO_PRIORIDADE SET RESUMO = ? WHERE ID = ?";

    private static final String SQL_DELETAR =
            "DELETE FROM RELATORIO_PRIORIDADE WHERE ID = ?";

    /** Registro histórico de uma execução do relatório de prioridade. */
    public record RelatorioRegistrado(
            long id, Timestamp dataGeracao, int qtUrgente, int qtCritico,
            int qtAtencao, int qtNormal, String resumo) {
    }

    public RelatorioPrioridadeDAO() {
        // construtor padrão exigido pelo enunciado
    }

    /**
     * Salva o snapshot de uma execução do GeradorRelatorio.
     * Método chamado diretamente pelo GeradorRelatorio.gerarRelatorio(...).
     */
    public RelatorioRegistrado salvarRelatorio(int qtUrgente, int qtCritico, int qtAtencao,
                                               int qtNormal, String resumo) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERIR, new String[]{"ID"})) {
            stmt.setInt(1, qtUrgente);
            stmt.setInt(2, qtCritico);
            stmt.setInt(3, qtAtencao);
            stmt.setInt(4, qtNormal);
            stmt.setString(5, resumo);
            stmt.executeUpdate();

            long idGerado;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                idGerado = generatedKeys.getLong(1);
            }
            return buscarPorId(idGerado);
        }
    }

    public RelatorioRegistrado buscarPorId(long id) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearLinha(rs) : null;
            }
        }
    }

    public List<RelatorioRegistrado> listarTodas() throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        List<RelatorioRegistrado> relatorios = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_LISTAR_TODAS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                relatorios.add(mapearLinha(rs));
            }
        }
        return relatorios;
    }

    public boolean atualizar(long id, String novoResumo) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_ATUALIZAR)) {
            stmt.setString(1, novoResumo);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(long id) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETAR)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private RelatorioRegistrado mapearLinha(ResultSet rs) throws SQLException {
        return new RelatorioRegistrado(
                rs.getLong("ID"),
                rs.getTimestamp("DATA_GERACAO"),
                rs.getInt("QT_URGENTE"),
                rs.getInt("QT_CRITICO"),
                rs.getInt("QT_ATENCAO"),
                rs.getInt("QT_NORMAL"),
                rs.getString("RESUMO")
        );
    }
}