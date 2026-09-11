package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerencia a conexão com o banco Oracle usando o driver ojdbc17
 * (JDBC puro — sem JPA/Hibernate, conforme pedido na Sprint 3).
 *
 * Implementa o padrão Singleton: existe apenas uma instância de ConexaoBD
 * em toda a aplicação, acessada via {@link #getInstancia()}.
 *
 * Por padrão, aponta para uma instalação LOCAL do Oracle XE (Express Edition),
 * padrão comum quando a FIAP distribui uma VM/instalação local para a
 * disciplina: host localhost, porta 1521, SID "XE". A URL usa o formato
 * clássico por SID: jdbc:oracle:thin:@host:porta:SID (com ":", não "//"+"/").
 *
 * Credenciais: NUNCA deixe usuário/senha fixos no código-fonte (evita subir
 * credenciais para o GitHub por engano). Configure as variáveis de ambiente
 * abaixo antes de rodar o Main (ou defina-as na Run Configuration da IDE):
 *
 *   ORACLE_HOST      -> ex: localhost   (padrão usado se não definida)
 *   ORACLE_PORT      -> ex: 1521        (padrão usado se não definida)
 *   ORACLE_SID       -> ex: XE          (padrão usado se não definida)
 *   ORACLE_USER      -> seu usuário/RM, ex: 564878
 *   ORACLE_PASSWORD  -> sua senha do Oracle
 *
 * Exemplo (IntelliJ): Run > Edit Configurations > Environment variables:
 *   ORACLE_USER=564878;ORACLE_PASSWORD=SuaSenhaAqui
 *
 * Se no seu ambiente o Oracle usar SERVICE_NAME em vez de SID (comum em
 * servidores remotos de laboratório), troque a montagem da URL abaixo para
 * o formato "jdbc:oracle:thin:@//host:porta/service".
 */
public final class ConexaoBD {

    private static final String HOST     = System.getenv().getOrDefault("ORACLE_HOST", "localhost");
    private static final String PORT     = System.getenv().getOrDefault("ORACLE_PORT", "1521");
    private static final String SID      = System.getenv().getOrDefault("ORACLE_SID", "XE");
    private static final String USER     = System.getenv().get("ORACLE_USER");
    private static final String PASSWORD = System.getenv().get("ORACLE_PASSWORD");

    private static final String URL = "jdbc:oracle:thin:@//" + HOST + ":" + PORT + "/" + SID;

    private static ConexaoBD instancia;

    private Connection connection;

    // Construtor privado — só a própria classe pode criar a instância (Singleton)
    private ConexaoBD() {
    }

    /**
     * Retorna a única instância de ConexaoBD da aplicação, criando-a na
     * primeira chamada (lazy initialization).
     */
    public static synchronized ConexaoBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexaoBD();
        }
        return instancia;
    }

    /**
     * Abre (ou reaproveita) a conexão com o Oracle.
     *
     * @return a Connection ativa
     * @throws SQLException se as credenciais estiverem erradas, o driver não
     *                       for encontrado ou o banco estiver inacessível
     */
    public Connection conectar() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        if (USER == null || PASSWORD == null) {
            throw new SQLException(
                    "Credenciais não configuradas. Defina as variáveis de ambiente " +
                            "ORACLE_USER e ORACLE_PASSWORD antes de executar o Main."
            );
        }

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver oracle.jdbc.driver.OracleDriver não encontrado no classpath. " +
                    "Verifique se lib/ojdbc17.jar foi adicionado ao projeto.", e);
        }

        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("✔ Conectado ao Oracle em " + URL + " como " + USER);
        return connection;
    }

    /** Retorna a conexão atual sem tentar abrir uma nova (pode ser nula). */
    public Connection getConnection() {
        return connection;
    }

    /** Fecha a conexão, se estiver aberta. Deve ser chamado ao final do programa. */
    public void desconectar() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("✔ Conexão com o Oracle encerrada.");
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}