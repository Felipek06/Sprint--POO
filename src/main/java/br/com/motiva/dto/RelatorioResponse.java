package br.com.motiva.dto;

import br.com.motiva.model.RelatorioPrioridade;

import java.time.LocalDateTime;

/**
 * Uma linha do histórico do motor de prioridade.
 *
 * É o que sai em GET /api/relatorios e em
 * GET /api/relatorios/periodo?inicio=...&fim=...
 */
public record RelatorioResponse(
        Long id,
        LocalDateTime dataGeracao,
        Integer qtUrgente,
        Integer qtCritico,
        Integer qtAtencao,
        Integer qtNormal,
        Integer totalTrechosAnalisados,
        String resumo
) {

    public static RelatorioResponse de(RelatorioPrioridade relatorio) {
        return new RelatorioResponse(
                relatorio.getId(),
                relatorio.getDataGeracao(),
                relatorio.getQtUrgente(),
                relatorio.getQtCritico(),
                relatorio.getQtAtencao(),
                relatorio.getQtNormal(),
                relatorio.getTotalTrechosAnalisados(),
                relatorio.getResumo());
    }
}
