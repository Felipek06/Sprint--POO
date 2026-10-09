package br.com.motiva.dto;

import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.model.TrechoSeco;
import br.com.motiva.model.TrechoUmido;
import br.com.motiva.model.TrechoUmidoMonitorado;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representação de um trecho no corpo das respostas da API.
 *
 * Observe que a resposta carrega dados DERIVADOS que não existem em coluna
 * nenhuma: taxaCrescimentoDiarioCm, critico, prioridade e recomendacao são
 * calculados pelo polimorfismo da Sprint 2 no momento da serialização. É
 * assim que o motor de regras continua visível na API sem que nada disso
 * precise ser gravado no banco.
 *
 * Os campos específicos de subtipo ficam fora do JSON quando são nulos
 * (@JsonInclude(NON_NULL)), então um trecho SECO não devolve
 * indicePluviometrico e um trecho ÚMIDO não devolve idSensor.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrechoResponse(
        Long id,
        String tipo,
        String descricaoTipo,
        Integer quilometroInicial,
        Integer quilometroFinal,
        Double nivelVegetacaoCm,
        Double taxaCrescimentoDiarioCm,
        boolean critico,
        String prioridade,
        String recomendacao,
        Double indicePluviometrico,
        Boolean emEstacaoSeca,
        String idSensor,
        EquipeResponse equipeResponsavel
) {

    /**
     * Converte a entidade (e qualquer uma de suas subclasses) para o DTO.
     *
     * Os instanceof abaixo existem só para expor os campos exclusivos de
     * cada subtipo no JSON. A lógica de negócio em si continua polimórfica:
     * calcularTaxaCrescimentoDiario() e classificarPrioridade() são
     * chamados na classe abstrata, sem nenhum teste de tipo.
     */
    public static TrechoResponse de(TrechoRodovia trecho) {
        Double indicePluviometrico = null;
        Boolean emEstacaoSeca = null;
        String idSensor = null;

        if (trecho instanceof TrechoUmido umido) {
            indicePluviometrico = umido.getIndicePluviometrico();
        }
        if (trecho instanceof TrechoSeco seco) {
            emEstacaoSeca = seco.isEmEstacaoSeca();
        }
        if (trecho instanceof TrechoUmidoMonitorado monitorado) {
            idSensor = monitorado.getIdSensor();
        }

        return new TrechoResponse(
                trecho.getId(),
                trecho.getTipo(),
                trecho.getDescricaoTipo(),
                trecho.getQuilometroInicial(),
                trecho.getQuilometroFinal(),
                trecho.getNivelVegetacaoCm(),
                trecho.calcularTaxaCrescimentoDiario(),
                trecho.isCritico(),
                trecho.classificarPrioridade().name(),
                trecho.classificarPrioridade().getRecomendacao(),
                indicePluviometrico,
                emEstacaoSeca,
                idSensor,
                EquipeResponse.de(trecho.getEquipeResponsavel()));
    }
}
