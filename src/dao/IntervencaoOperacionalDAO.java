package dao;

import db.ConexaoBD;
import model.IntervencaoOperacional;
import model.Pulverizacao;
import model.RocadaMecanizada;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo CRUD da tabela INTERVENCAO_OPERACIONAL.
 *
 * Diferente de TrechoRodoviaDAO, aqui uma intervenção já executada
 * ({@link IntervencaoOperacional#executarServico()} já rodou) é apenas um
 * registro histórico — não precisamos reconstruir um objeto RocadaMecanizada
 * ou Pulverizacao "vivo" ao consultar o banco. Por isso o record
 * {@link IntervencaoRegistrada} é o próprio tipo de retorno público do DAO.
 */
public class IntervencaoOperacionalDAO {

    private static final String TIPO_ROCADA = "ROCADA_MECANIZADA";
    private static final String TIPO_PULVERIZACAO = "PULVERIZACAO";

    private static final String SQL_INSERIR =
            "INSERT INTO INTERVENCAO_OPERACIONAL " +
                    "(ID_TRECHO_ALVO, ID_EQUIPE_RESPONSAVEL, TIPO_INTERVENCAO, TIPO_PRODUTO) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT ID, ID_TRECHO_ALVO, ID_EQUIPE_RESPONSAVEL, TIPO_INTERVENCAO, TIPO_PRODUTO, DATA_EXECUCAO " +
                    "FROM INTERVENCAO_OPERACIONAL WHERE ID = ?";

    private static final String SQL_LISTAR_TODAS =
            "SELECT ID, ID_TRECHO_ALVO, ID_EQUIPE_RESPONSAVEL, TIPO_INTERVENCAO, TIPO_PRODUTO, DATA_EXECUCAO " +
                    "FROM INTERVENCAO_OPERACIONAL ORDER BY ID";

    private static final String SQL_ATUALIZAR_EQUIPE =
            "UPDATE INTERVENCAO_OPERACIONAL SET ID_EQUIPE_RESPONSAVEL = ? WHERE ID = ?";

    private static final String SQL_DELETAR =
            "DELETE FROM INTERVENCAO_OPERACIONAL WHERE ID = ?";

    /** Registro de uma intervenção já persistida no histórico. */
    public record IntervencaoRegistrada(
            long id, long idTrechoAlvo, long idEquipeResponsavel,
            String tipoIntervencao, String tipoProduto, Timestamp dataExecucao) {
    }

    public IntervencaoOperacionalDAO() {
        // construtor padrão exigido pelo enunciado
    }

    /**
     * Persiste o histórico de uma intervenção já executada.
     * Requer que o trecho alvo e a equipe responsável já tenham sido
     * salvos anteriormente (precisam de id).
     */
    public IntervencaoRegistrada inserir(IntervencaoOperacional intervencao) throws SQLException {
        Long idTrecho = intervencao.getTrechoAlvo().getId();
        Long idEquipe = intervencao.getEquipeResponsavel().getId();
        if (idTrecho == null || idEquipe == null) {
            throw new IllegalArgumentException(
                    "O trecho e a equipe da intervenção precisam estar persistidos (com id) antes de registrar a intervenção.");
        }

        String tipoIntervencao = (intervencao instanceof RocadaMecanizada) ? TIPO_ROCADA : TIPO_PULVERIZACAO;
        String tipoProduto = (intervencao instanceof Pulverizacao pulverizacao)
                ? pulverizacao.getTipoProduto().name()
                : null;

        Connection conn = ConexaoBD.getInstancia().conectar();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERIR, new String[]{"ID"})) {
            stmt.setLong(1, idTrecho);
            stmt.setLong(2, idEquipe);
            stmt.setString(3, tipoIntervencao);
            if (tipoProduto != null) {
                stmt.setString(4, tipoProduto);
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            stmt.executeUpdate();

            long idGerado;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                generatedKeys.next();
                idGerado = generatedKeys.getLong(1);
            }
            return buscarPorId(idGerado);
        }
    }

    public IntervencaoRegistrada buscarPorId(long id) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearLinha(rs) : null;
            }
        }
    }

    public List<IntervencaoRegistrada> listarTodas() throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        List<IntervencaoRegistrada> intervencoes = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_LISTAR_TODAS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                intervencoes.add(mapearLinha(rs));
            }
        }
        return intervencoes;
    }

    /** Reatribui a intervenção a outra equipe responsável. */
    public boolean atualizar(long id, long novoIdEquipeResponsavel) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_ATUALIZAR_EQUIPE)) {
            stmt.setLong(1, novoIdEquipeResponsavel);
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

    private IntervencaoRegistrada mapearLinha(ResultSet rs) throws SQLException {
        return new IntervencaoRegistrada(
                rs.getLong("ID"),
                rs.getLong("ID_TRECHO_ALVO"),
                rs.getLong("ID_EQUIPE_RESPONSAVEL"),
                rs.getString("TIPO_INTERVENCAO"),
                rs.getString("TIPO_PRODUTO"),
                rs.getTimestamp("DATA_EXECUCAO")
        );
    }
}