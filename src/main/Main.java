package main;

import dao.EquipeManutencaoDAO;
import dao.IntervencaoOperacionalDAO;
import dao.RelatorioPrioridadeDAO;
import dao.TrechoRodoviaDAO;
import db.ConexaoBD;
import model.EquipeManutencao;
import model.Pulverizacao;
import model.RocadaMecanizada;
import model.TrechoRodovia;
import model.TrechoSeco;
import model.TrechoUmido;
import model.TrechoUmidoMonitorado;
import service.GeradorRelatorio;

import java.sql.SQLException;
import java.util.List;

/**
 * Sprint 3 — Persistência com Oracle e JDBC puro.
 *
 * Demonstra, em sequência: conexão com o banco, CRUD de EquipeManutencao,
 * CRUD de TrechoRodovia (hierarquia persistida em uma única tabela), CRUD
 * de IntervencaoOperacional, geração de relatório com persistência do
 * histórico e, por fim, a consulta desse histórico.
 *
 * Pré-requisito: rode sql/create_tables.sql no Oracle e configure as
 * variáveis de ambiente ORACLE_USER / ORACLE_PASSWORD (veja db.ConexaoBD).
 */
public class Main {

    private static final String SEPARADOR = "─".repeat(65);

    public static void main(String[] args) {
        System.out.println(SEPARADOR);
        System.out.println("  SPRINT 3 — PERSISTÊNCIA COM ORACLE E JDBC PURO");
        System.out.println(SEPARADOR);

        ConexaoBD conexao = ConexaoBD.getInstancia();

        try {
            // 1. Testar conexão
            titulo("1. Testando conexão com o Oracle");
            conexao.conectar();
            sucesso("Conexão estabelecida.");

            // 2. Testar CRUD de Equipe
            List<EquipeManutencao> equipes = testarCrudEquipe();

            // 3. Testar CRUD de Trechos
            List<TrechoRodovia> trechos = testarCrudTrecho(equipes);

            // 4. Testar CRUD de Intervenções + geração do relatório
            testarIntervencoesERelatorio(trechos, equipes);

            // 5. Consultar histórico de relatórios
            testarHistoricoRelatorios();

        } catch (SQLException e) {
            falha("Erro SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            falha("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 7. Fechar conexão
            conexao.desconectar();
        }

        System.out.println("\n" + SEPARADOR);
        System.out.println("  FIM DA SPRINT 3");
        System.out.println(SEPARADOR);
    }

    // =========================================================================
    // 2. CRUD de EquipeManutencao
    // =========================================================================

    private static List<EquipeManutencao> testarCrudEquipe() throws SQLException {
        titulo("2. CRUD de EquipeManutencao");
        EquipeManutencaoDAO daoEquipe = new EquipeManutencaoDAO();

        EquipeManutencao alpha = daoEquipe.inserir(new EquipeManutencao("Equipe Alpha", 6));
        EquipeManutencao beta = daoEquipe.inserir(new EquipeManutencao("Equipe Beta", 4));
        System.out.println("Inseridas: " + alpha + " (id=" + alpha.getId() + "), " +
                beta + " (id=" + beta.getId() + ")");

        EquipeManutencao encontrada = daoEquipe.buscarPorId(alpha.getId());
        System.out.println("buscarPorId(" + alpha.getId() + "): " + encontrada);

        System.out.println("listarTodas():");
        daoEquipe.listarTodas().forEach(e -> System.out.println("  - " + e + " (id=" + e.getId() + ")"));

        boolean atualizou = daoEquipe.atualizar(beta.getId(), "Equipe Beta Reforçada", 5);
        System.out.println("atualizar(" + beta.getId() + "): " + atualizou);

        // Demonstra deletar() com uma equipe descartável, sem vínculos de FK
        EquipeManutencao descartavel = daoEquipe.inserir(new EquipeManutencao("Equipe Temporária", 2));
        boolean deletou = daoEquipe.deletar(descartavel.getId());
        System.out.println("deletar(" + descartavel.getId() + "): " + deletou);

        sucesso("CRUD de EquipeManutencao validado.");
        return List.of(alpha, beta);
    }

    // =========================================================================
    // 3. CRUD de TrechoRodovia
    // =========================================================================

    private static List<TrechoRodovia> testarCrudTrecho(List<EquipeManutencao> equipes) throws SQLException {
        titulo("3. CRUD de TrechoRodovia (Úmido / Seco / Úmido Monitorado)");
        TrechoRodoviaDAO daoTrecho = new TrechoRodoviaDAO();
        EquipeManutencao alpha = equipes.get(0);
        EquipeManutencao beta = equipes.get(1);

        TrechoUmidoMonitorado monitorado = new TrechoUmidoMonitorado(0, 10, 20.0, 1.0, "SENSOR-BR116-KM05");
        TrechoUmido umido = new TrechoUmido(10, 20, 10.0, 1.8);
        TrechoSeco seco = new TrechoSeco(20, 30, 5.0, false);
        TrechoUmido critico = new TrechoUmido(30, 40, 50.0, 1.0);
        TrechoSeco urgente = new TrechoSeco(40, 50, 70.0, false);

        // Simula alguns dias de crescimento (Sprint 2 — motor de regras)
        monitorado.simularCrescimento(15);
        umido.simularCrescimento(20);
        seco.simularCrescimento(30);
        critico.simularCrescimento(5);
        urgente.simularCrescimento(5);

        critico.associarEquipe(alpha);
        urgente.associarEquipe(beta);

        for (TrechoRodovia t : List.of(monitorado, umido, seco, critico, urgente)) {
            daoTrecho.inserir(t);
        }
        System.out.println("Inseridos 5 trechos. Exemplo: " + urgente + " (id=" + urgente.getId() + ")");

        TrechoRodovia encontrado = daoTrecho.buscarPorId(urgente.getId());
        System.out.println("buscarPorId(" + urgente.getId() + "): " + encontrado);

        List<TrechoRodovia> todos = daoTrecho.listarTodas();
        System.out.println("listarTodas() — " + todos.size() + " trecho(s) no banco:");
        todos.forEach(t -> System.out.println("  - " + t + " (id=" + t.getId() + ")"));

        boolean atualizou = daoTrecho.atualizar(seco);
        System.out.println("atualizar(" + seco.getId() + "): " + atualizou);

        sucesso("CRUD de TrechoRodovia validado.");
        return todos;
    }

    // =========================================================================
    // 4. CRUD de IntervencaoOperacional + Relatório de Prioridade
    // =========================================================================

    private static void testarIntervencoesERelatorio(List<TrechoRodovia> trechos,
                                                     List<EquipeManutencao> equipes) throws SQLException {
        titulo("4. Intervenções operacionais e Relatório de Prioridade");
        IntervencaoOperacionalDAO daoIntervencao = new IntervencaoOperacionalDAO();
        EquipeManutencao beta = equipes.get(1);
        EquipeManutencao alpha = equipes.get(0);

        TrechoRodovia trechoUrgente = trechos.stream()
                .filter(t -> t.getNivelVegetacaoCm() >= 70)
                .findFirst().orElse(trechos.get(trechos.size() - 1));
        TrechoRodovia trechoCritico = trechos.stream()
                .filter(t -> t.getNivelVegetacaoCm() >= 50 && t != trechoUrgente)
                .findFirst().orElse(trechos.get(0));

        RocadaMecanizada rocada = new RocadaMecanizada(trechoUrgente, beta);
        rocada.executarServico();
        var registroRocada = daoIntervencao.inserir(rocada);
        System.out.println("Intervenção registrada no banco: " + registroRocada);

        Pulverizacao pulverizacao = new Pulverizacao(trechoCritico, alpha, Pulverizacao.TipoProduto.HERBICIDA_SELETIVO);
        pulverizacao.executarServico();
        var registroPulverizacao = daoIntervencao.inserir(pulverizacao);
        System.out.println("Intervenção registrada no banco: " + registroPulverizacao);

        System.out.println("listarTodas() intervenções:");
        daoIntervencao.listarTodas().forEach(i -> System.out.println("  - " + i));

        sucesso("CRUD de IntervencaoOperacional validado.");

        // Gera o relatório com o estado atual dos trechos e já persiste o histórico
        titulo("4b. Geração do Relatório de Prioridade (com persistência do histórico)");
        GeradorRelatorio gerador = new GeradorRelatorio();
        gerador.gerarRelatorio(trechos.toArray(new TrechoRodovia[0]));
    }

    // =========================================================================
    // 5. Histórico de relatórios
    // =========================================================================

    private static void testarHistoricoRelatorios() throws SQLException {
        titulo("5. Histórico de relatórios (RELATORIO_PRIORIDADE)");
        RelatorioPrioridadeDAO daoRelatorio = new RelatorioPrioridadeDAO();
        List<RelatorioPrioridadeDAO.RelatorioRegistrado> historico = daoRelatorio.listarTodas();
        System.out.println(historico.size() + " relatório(s) no histórico:");
        historico.forEach(r -> System.out.println("  - " + r));
        sucesso("Histórico consultado com sucesso.");
    }

    // =========================================================================
    // Utilitários de exibição
    // =========================================================================

    private static void titulo(String descricao) {
        System.out.println("\n" + SEPARADOR);
        System.out.println("  " + descricao);
        System.out.println(SEPARADOR);
    }

    private static void sucesso(String mensagem) {
        System.out.println("✔ " + mensagem);
    }

    private static void falha(String mensagem) {
        System.out.println("✘ " + mensagem);
    }
}