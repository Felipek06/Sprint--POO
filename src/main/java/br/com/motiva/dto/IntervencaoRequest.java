package br.com.motiva.dto;

import br.com.motiva.model.Pulverizacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corpo aceito em POST e PUT de /api/intervencoes.
 *
 * O cliente não envia o objeto trecho nem o objeto equipe inteiros: envia
 * apenas os ids. Quem transforma id em entidade — e quem devolve 404 se o
 * id não existir — é o Service.
 */
public record IntervencaoRequest(

        @NotNull(message = "O tipo da intervencao e obrigatorio: ROCADA_MECANIZADA ou PULVERIZACAO.")
        TipoIntervencao tipo,

        @NotNull(message = "O id do trecho alvo e obrigatorio.")
        @Positive(message = "O id do trecho alvo deve ser positivo.")
        Long trechoAlvoId,

        @NotNull(message = "O id da equipe responsavel e obrigatorio.")
        @Positive(message = "O id da equipe responsavel deve ser positivo.")
        Long equipeResponsavelId,

        /** Obrigatório para PULVERIZACAO; ignorado para ROCADA_MECANIZADA. */
        Pulverizacao.TipoProduto tipoProduto
) {
}
