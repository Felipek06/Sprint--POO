package dao;

import db.ConexaoBD;
import model.EquipeManutencao;
import model.TrechoRodovia;
import model.TrechoSeco;
import model.TrechoUmido;
import model.TrechoUmidoMonitorado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo CRUD da tabela TRECHO_RODOVIA.
 *
 * A hierarquia TrechoRodovia (abstrata) / TrechoUmido / TrechoSeco /
 * TrechoUmidoMonitorado é persistida em uma única tabela, diferenciada
 * pela coluna TIPO_TRECHO ("UMIDO", "SECO", "UMIDO_MONITORADO") — estratégia
 * conhecida como Single Table Inheritance.
 *
 * Internamente cada linha é lida para um record (TrechoRodoviaRow) e depois
 * reconstruída na subclasse concreta correta, de forma que
 * {@link #listarTodas()} devolve trechos totalmente funcionais — prontos
 * para alimentar o GeradorRelatorio, exatamente como no protótipo de Main
 * do enunciado.
 */
public class TrechoRodoviaDAO {

    private static final String TIPO_UMIDO = "UMIDO";
    private static final String TIPO_SECO = "SECO";
    private static final String TIPO_UMIDO_MONITORADO = "UMIDO_MONITORADO";

    private static final String SQL_INSERIR =
            "INSERT INTO TRECHO_RODOVIA " +
                    "(QUILOMETRO_INICIAL, QUILOMETRO_FINAL, NIVEL_VEGETACAO_CM, TIPO_TRECHO, " +
                    " INDICE_PLUVIOMETRICO, EM_ESTACAO_SECA, ID_SENSOR, ID_EQUIPE_RESPONSAVEL) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT ID, QUILOMETRO_INICIAL, QUILOMETRO_FINAL, NIVEL_VEGETACAO_CM, TIPO_TRECHO, " +
                    "       INDICE_PLUVIOMETRICO, EM_ESTACAO_SECA, ID_SENSOR, ID_EQUIPE_RESPONSAVEL " +
                    "FROM TRECHO_RODOVIA WHERE ID = ?";

    private static final String SQL_LISTAR_TODAS =
            "SELECT ID, QUILOMETRO_INICIAL, QUILOMETRO_FINAL, NIVEL_VEGETACAO_CM, TIPO_TRECHO, " +
                    "       INDICE_PLUVIOMETRICO, EM_ESTACAO_SECA, ID_SENSOR, ID_EQUIPE_RESPONSAVEL " +
                    "FROM TRECHO_RODOVIA ORDER BY ID";

    private static final String SQL_ATUALIZAR =
            "UPDATE TRECHO_RODOVIA SET NIVEL_VEGETACAO_CM = ?, ID_EQUIPE_RESPONSAVEL = ? WHERE ID = ?";

    private static final String SQL_DELETAR =
            "DELETE FROM TRECHO_RODOVIA WHERE ID = ?";

    /** Representação crua de uma linha da tabela TRECHO_RODOVIA. */
    private record TrechoRodoviaRow(
            long id, int quilometroInicial, int quilometroFinal, double nivelVegetacaoCm,
            String tipoTrecho, Double indicePluviometrico, String emEstacaoSeca,
            String idSensor, Long idEquipeResponsavel) {
    }

    private final EquipeManutencaoDAO equipeDAO = new EquipeManutencaoDAO();

    public TrechoRodoviaDAO() {
        // construtor padrão exigido pelo enunciado
    }

    /**
     * Insere um trecho no banco. Se o trecho já tiver uma equipe associada
     * (associarEquipe), essa equipe precisa já estar persistida — ou seja,
     * já ter um id — para satisfazer a FK ID_EQUIPE_RESPONSAVEL.
     */
    public TrechoRodovia inserir(TrechoRodovia trecho) throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERIR, new String[]{"ID"})) {
            preencherParametrosComuns(stmt, trecho);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    trecho.setId(generatedKeys.getLong(1));
                }
            }
        }
        return trecho;
    }

    public TrechoRodovia buscarPorId(Long id) throws SQLException {
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

    public List<TrechoRodovia> listarTodas() throws SQLException {
        Connection conn = ConexaoBD.getInstancia().conectar();
        List<TrechoRodovia> trechos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_LISTAR_TODAS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                trechos.add(paraDominio(mapearLinha(rs)));
            }
        }
        return trechos;
    }

    /**
     * Atualiza o nível de vegetação e a equipe responsável de um trecho
     * já persistido (id não pode ser nulo).
     */
    public boolean atualizar(TrechoRodovia trecho) throws SQLException {
        if (trecho.getId() == null) {
            throw new IllegalArgumentException("Não é possível atualizar um trecho sem id (ainda não persistido).");
        }
        Connection conn = ConexaoBD.getInstancia().conectar();

        try (PreparedStatement stmt = conn.prepareStatement(SQL_ATUALIZAR)) {
            stmt.setDouble(1, trecho.getNivelVegetacaoCm());
            Long idEquipe = trecho.getEquipeResponsavel() != null ? trecho.getEquipeResponsavel().getId() : null;
            if (idEquipe != null) {
                stmt.setLong(2, idEquipe);
            } else {
                stmt.setNull(2, Types.NUMERIC);
            }
            stmt.setLong(3, trecho.getId());
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
    // Mapeamento de parâmetros na inserção
    // -------------------------------------------------------------------------

    private void preencherParametrosComuns(PreparedStatement stmt, TrechoRodovia trecho) throws SQLException {
        stmt.setInt(1, trecho.getQuilometroInicial());
        stmt.setInt(2, trecho.getQuilometroFinal());
        stmt.setDouble(3, trecho.getNivelVegetacaoCm());

        if (trecho instanceof TrechoUmidoMonitorado monitorado) {
            stmt.setString(4, TIPO_UMIDO_MONITORADO);
            stmt.setDouble(5, monitorado.getIndicePluviometrico());
            stmt.setNull(6, Types.CHAR);
            stmt.setString(7, monitorado.getIdSensor());
        } else if (trecho instanceof TrechoUmido umido) {
            stmt.setString(4, TIPO_UMIDO);
            stmt.setDouble(5, umido.getIndicePluviometrico());
            stmt.setNull(6, Types.CHAR);
            stmt.setNull(7, Types.VARCHAR);
        } else if (trecho instanceof TrechoSeco seco) {
            stmt.setString(4, TIPO_SECO);
            stmt.setNull(5, Types.NUMERIC);
            stmt.setString(6, seco.isEmEstacaoSeca() ? "S" : "N");
            stmt.setNull(7, Types.VARCHAR);
        } else {
            throw new IllegalArgumentException(
                    "Tipo de trecho desconhecido: " + trecho.getClass().getSimpleName());
        }

        Long idEquipe = trecho.getEquipeResponsavel() != null ? trecho.getEquipeResponsavel().getId() : null;
        if (idEquipe != null) {
            stmt.setLong(8, idEquipe);
        } else {
            stmt.setNull(8, Types.NUMERIC);
        }
    }

    // -------------------------------------------------------------------------
    // Mapeamento ResultSet -> record -> objeto de domínio (polimórfico)
    // -------------------------------------------------------------------------

    private TrechoRodoviaRow mapearLinha(ResultSet rs) throws SQLException {
        Double indice = rs.getObject("INDICE_PLUVIOMETRICO") != null ? rs.getDouble("INDICE_PLUVIOMETRICO") : null;
        Long idEquipe = rs.getObject("ID_EQUIPE_RESPONSAVEL") != null ? rs.getLong("ID_EQUIPE_RESPONSAVEL") : null;
        return new TrechoRodoviaRow(
                rs.getLong("ID"),
                rs.getInt("QUILOMETRO_INICIAL"),
                rs.getInt("QUILOMETRO_FINAL"),
                rs.getDouble("NIVEL_VEGETACAO_CM"),
                rs.getString("TIPO_TRECHO"),
                indice,
                rs.getString("EM_ESTACAO_SECA"),
                rs.getString("ID_SENSOR"),
                idEquipe
        );
    }

    private TrechoRodovia paraDominio(TrechoRodoviaRow row) throws SQLException {
        TrechoRodovia trecho = switch (row.tipoTrecho()) {
            case TIPO_UMIDO_MONITORADO -> new TrechoUmidoMonitorado(
                    row.quilometroInicial(), row.quilometroFinal(), row.nivelVegetacaoCm(),
                    row.indicePluviometrico() != null ? row.indicePluviometrico() : 1.0,
                    row.idSensor()
            );
            case TIPO_UMIDO -> new TrechoUmido(
                    row.quilometroInicial(), row.quilometroFinal(), row.nivelVegetacaoCm(),
                    row.indicePluviometrico() != null ? row.indicePluviometrico() : 1.0
            );
            case TIPO_SECO -> new TrechoSeco(
                    row.quilometroInicial(), row.quilometroFinal(), row.nivelVegetacaoCm(),
                    "S".equals(row.emEstacaoSeca())
            );
            default -> throw new SQLException("TIPO_TRECHO desconhecido no banco: " + row.tipoTrecho());
        };

        trecho.setId(row.id());

        if (row.idEquipeResponsavel() != null) {
            EquipeManutencao equipe = equipeDAO.buscarPorId(row.idEquipeResponsavel());
            if (equipe != null) {
                trecho.associarEquipe(equipe);
            }
        }
        return trecho;
    }
}