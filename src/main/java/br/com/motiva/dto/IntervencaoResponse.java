package br.com.motiva.dto;

import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.Pulverizacao;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Representação de uma intervenção no corpo das respostas da API.
 *
 * O campo resultadoExecucao só aparece na resposta do POST, porque ele é o
 * retorno polimórfico de executarServico() — o texto que, na Sprint 2, ia
 * para o console. Nos GETs ele vem nulo e é omitido do JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record IntervencaoResponse(
        Long id,
        String tipo,
        String descricaoTipo,
        LocalDateTime dataExecucao,
        String tipoProduto,
        TrechoResumo trechoAlvo,
        EquipeResponse equipeResponsavel,
        String resultadoExecucao
) {

    /** Resumo do trecho, para não aninhar o trecho inteiro dentro da intervenção. */
    public record TrechoResumo(
            Long id,
            String descricaoTipo,
            Integer quilometroInicial,
            Integer quilometroFinal,
            Double nivelVegetacaoCm
    ) {
    }

    public static IntervencaoResponse de(IntervencaoOperacional intervencao) {
        return de(intervencao, null);
    }

    public static IntervencaoResponse de(IntervencaoOperacional intervencao, String resultadoExecucao) {
        String tipoProduto = (intervencao instanceof Pulverizacao pulverizacao
                && pulverizacao.getTipoProduto() != null)
                ? pulverizacao.getTipoProduto().name()
                : null;

        TrechoResumo trecho = new TrechoResumo(
                intervencao.getTrechoAlvo().getId(),
                intervencao.getTrechoAlvo().getDescricaoTipo(),
                intervencao.getTrechoAlvo().getQuilometroInicial(),
                intervencao.getTrechoAlvo().getQuilometroFinal(),
                intervencao.getTrechoAlvo().getNivelVegetacaoCm());

        return new IntervencaoResponse(
                intervencao.getId(),
                intervencao.getTipo(),
                intervencao.getDescricaoTipo(),
                intervencao.getDataExecucao(),
                tipoProduto,
                trecho,
                EquipeResponse.de(intervencao.getEquipeResponsavel()),
                resultadoExecucao);
    }
}
