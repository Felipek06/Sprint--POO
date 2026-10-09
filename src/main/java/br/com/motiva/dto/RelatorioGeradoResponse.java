package br.com.motiva.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Resposta de POST /api/relatorios.
 *
 * Além do snapshot que foi gravado no histórico, devolve o detalhamento
 * trecho a trecho da execução — o equivalente, em JSON, àquele bloco que a
 * Sprint 3 desenhava no console com caracteres de moldura.
 */
public record RelatorioGeradoResponse(
        RelatorioResponse relatorio,
        List<ItemClassificado> itens
) {

    /**
     * Classificação de um trecho dentro desta execução do relatório.
     *
     * @param leituraSensorCm leitura bruta do sensor IoT; vem nula para
     *                        trechos que não são monitorados
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ItemClassificado(
            Long trechoId,
            String descricaoTipo,
            Integer quilometroInicial,
            Integer quilometroFinal,
            Double nivelVegetacaoCm,
            Double leituraSensorCm,
            String prioridade,
            String intervencaoRecomendada
    ) {
    }
}
