package service;

import dao.RelatorioPrioridadeDAO;
import model.MonitoravelViaIoT;
import model.TrechoRodovia;

import java.sql.SQLException;

/**
 * Motor de geração do Relatório de Prioridade de Roçada.
 *
 * Varre um array de trechos e classifica cada um por nível de urgência,
 * indicando o tipo de intervenção recomendado.
 *
 * Lógica de priorização:
 *  nivelVegetacao >= 80 cm → URGENTE   → Roçada Mecanizada
 *  nivelVegetacao >= 50 cm → CRÍTICO   → Pulverização + possível Mecanizada
 *  nivelVegetacao >= 25 cm → ATENÇÃO   → Monitorar / agendar intervenção
 *  nivelVegetacao <  25 cm → NORMAL    → Sem intervenção necessária
 *
 * Para trechos IoT, o relatório consulta o sensor antes de classificar.
 *
 * Sprint 3: além de imprimir no console, cada execução agora é persistida
 * no histórico da tabela RELATORIO_PRIORIDADE através do RelatorioPrioridadeDAO.
 */
public class GeradorRelatorio {

    private static final String SEPARADOR       = "═".repeat(65);
    private static final String SEPARADOR_LINHA = "─".repeat(65);

    // Limites de classificação
    private static final double LIMITE_URGENTE  = 80.0;
    private static final double LIMITE_CRITICO  = 50.0;
    private static final double LIMITE_ATENCAO  = 25.0;

    /**
     * Gera e imprime o Relatório de Prioridade para um conjunto de trechos.
     *
     * @param trechos array de trechos a analisar (não pode ser nulo ou vazio)
     */
    public void gerarRelatorio(TrechoRodovia[] trechos) {
        validarEntrada(trechos);

        imprimirCabecalho();
        atualizarTrechosIoT(trechos);
        imprimirCorpoRelatorio(trechos);
        imprimirResumo(trechos);
        salvarHistorico(trechos);
    }

    // -------------------------------------------------------------------------
    // Etapas do relatório
    // -------------------------------------------------------------------------

    private void atualizarTrechosIoT(TrechoRodovia[] trechos) {
        boolean temIoT = false;
        for (TrechoRodovia trecho : trechos) {
            if (trecho instanceof MonitoravelViaIoT sensor) {
                if (!temIoT) {
                    System.out.println("📡 Coletando dados dos sensores IoT...");
                    temIoT = true;
                }
                // Atualiza o crescimento com a leitura do sensor
                double leitura = sensor.transmitirDadosSensor();
                double diferenca = leitura - trecho.getNivelVegetacaoCm();
                if (diferenca > 0) {
                    trecho.registrarCrescimento(diferenca);
                }
            }
        }
        if (temIoT) System.out.println();
    }

    private void imprimirCabecalho() {
        System.out.println("\n" + SEPARADOR);
        System.out.println("   RELATÓRIO DE PRIORIDADE DE ROÇADA — MOTIVA");
        System.out.println(SEPARADOR);
    }

    private void imprimirCorpoRelatorio(TrechoRodovia[] trechos) {
        System.out.println("\n TRECHOS ANALISADOS:\n");

        for (int i = 0; i < trechos.length; i++) {
            TrechoRodovia trecho     = trechos[i];
            Prioridade    prioridade = classificar(trecho);

            System.out.printf(" %2d. KM %3d → KM %3d | %-30s | %6.1f cm | %s%n",
                    i + 1,
                    trecho.getQuilometroInicial(),
                    trecho.getQuilometroFinal(),
                    trecho.getTipoTrecho(),
                    trecho.getNivelVegetacaoCm(),
                    prioridade.rotulo()
            );
            System.out.printf("     Intervenção recomendada: %s%n",
                    recomendar(trecho, prioridade));

            if (i < trechos.length - 1) System.out.println("     " + SEPARADOR_LINHA.substring(5));
        }
    }

    private void imprimirResumo(TrechoRodovia[] trechos) {
        long urgentes  = contarPorPrioridade(trechos, Prioridade.URGENTE);
        long criticos  = contarPorPrioridade(trechos, Prioridade.CRITICO);
        long atencao   = contarPorPrioridade(trechos, Prioridade.ATENCAO);
        long normais   = contarPorPrioridade(trechos, Prioridade.NORMAL);

        System.out.println("\n" + SEPARADOR);
        System.out.println(" RESUMO EXECUTIVO");
        System.out.println(SEPARADOR);
        System.out.printf(" 🔴 URGENTE  (Roçada Mecanizada imediata) : %d trecho(s)%n", urgentes);
        System.out.printf(" 🟠 CRÍTICO  (Pulverização prioritária)   : %d trecho(s)%n", criticos);
        System.out.printf(" 🟡 ATENÇÃO  (Agendar intervenção)        : %d trecho(s)%n", atencao);
        System.out.printf(" 🟢 NORMAL   (Sem ação necessária)        : %d trecho(s)%n", normais);
        System.out.println(SEPARADOR + "\n");
    }

    // -------------------------------------------------------------------------
    // Persistência do histórico (Sprint 3)
    // -------------------------------------------------------------------------

    /**
     * Salva o resultado desta execução do relatório na tabela RELATORIO_PRIORIDADE,
     * através do RelatorioPrioridadeDAO. Uma falha de banco não deve derrubar o
     * relatório já impresso no console — apenas é reportada.
     */
    private void salvarHistorico(TrechoRodovia[] trechos) {
        long urgentes = contarPorPrioridade(trechos, Prioridade.URGENTE);
        long criticos = contarPorPrioridade(trechos, Prioridade.CRITICO);
        long atencao  = contarPorPrioridade(trechos, Prioridade.ATENCAO);
        long normais  = contarPorPrioridade(trechos, Prioridade.NORMAL);

        String resumo = String.format(
                "%d trecho(s) analisado(s): %d urgente(s), %d crítico(s), %d em atenção, %d normal(is).",
                trechos.length, urgentes, criticos, atencao, normais
        );

        try {
            RelatorioPrioridadeDAO dao = new RelatorioPrioridadeDAO();
            dao.salvarRelatorio((int) urgentes, (int) criticos, (int) atencao, (int) normais, resumo);
            System.out.println(" 💾 Histórico do relatório salvo no banco (RELATORIO_PRIORIDADE).\n");
        } catch (SQLException e) {
            System.err.println(" ⚠ Não foi possível salvar o histórico do relatório: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Lógica de classificação e recomendação
    // -------------------------------------------------------------------------

    private Prioridade classificar(TrechoRodovia trecho) {
        double nivel = trecho.getNivelVegetacaoCm();
        if (nivel >= LIMITE_URGENTE)  return Prioridade.URGENTE;
        if (nivel >= LIMITE_CRITICO)  return Prioridade.CRITICO;
        if (nivel >= LIMITE_ATENCAO)  return Prioridade.ATENCAO;
        return Prioridade.NORMAL;
    }

    private String recomendar(TrechoRodovia trecho, Prioridade prioridade) {
        return switch (prioridade) {
            case URGENTE  -> "🔴 Roçada Mecanizada — despachar equipe IMEDIATAMENTE";
            case CRITICO  -> "🟠 Pulverização herbicida + reavaliar em 7 dias";
            case ATENCAO  -> "🟡 Agendar roçada manual nas próximas 2 semanas";
            case NORMAL   -> "🟢 Monitoramento de rotina";
        };
    }

    private long contarPorPrioridade(TrechoRodovia[] trechos, Prioridade alvo) {
        long count = 0;
        for (TrechoRodovia t : trechos) {
            if (classificar(t) == alvo) count++;
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // Validação de entrada
    // -------------------------------------------------------------------------

    private void validarEntrada(TrechoRodovia[] trechos) {
        if (trechos == null || trechos.length == 0) {
            throw new IllegalArgumentException(
                    "O array de trechos não pode ser nulo ou vazio."
            );
        }
    }

    // -------------------------------------------------------------------------
    // Enum interno de prioridade
    // -------------------------------------------------------------------------

    private enum Prioridade {
        URGENTE ("🔴 URGENTE"),
        CRITICO ("🟠 CRÍTICO"),
        ATENCAO ("🟡 ATENÇÃO"),
        NORMAL  ("🟢 NORMAL ");

        private final String label;
        Prioridade(String label) { this.label = label; }
        public String rotulo()   { return label; }
    }
}