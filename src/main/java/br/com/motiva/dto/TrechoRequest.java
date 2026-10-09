package br.com.motiva.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Corpo aceito em POST e PUT de /api/trechos.
 *
 * É um DTO só para os três tipos de trecho. Os campos específicos
 * (indicePluviometrico, emEstacaoSeca, idSensor) são opcionais aqui, e quem
 * decide quais deles são obrigatórios para cada tipo é o
 * TrechoRodoviaService — porque isso é regra de negócio, não formato.
 *
 * O Bean Validation cuida apenas do que dá para checar olhando um campo
 * isolado. Regras que comparam dois campos, como "o km final precisa ser
 * maior que o inicial", ficam no Service.
 */
public record TrechoRequest(

        @NotNull(message = "O tipo do trecho e obrigatorio: UMIDO, SECO ou UMIDO_MONITORADO.")
        TipoTrecho tipo,

        @NotNull(message = "O quilometro inicial e obrigatorio.")
        @PositiveOrZero(message = "O quilometro inicial nao pode ser negativo.")
        Integer quilometroInicial,

        @NotNull(message = "O quilometro final e obrigatorio.")
        @Positive(message = "O quilometro final deve ser maior que zero.")
        Integer quilometroFinal,

        @NotNull(message = "O nivel de vegetacao e obrigatorio.")
        @PositiveOrZero(message = "O nivel de vegetacao nao pode ser negativo.")
        Double nivelVegetacaoCm,

        /** Obrigatório para UMIDO e UMIDO_MONITORADO; ignorado para SECO. */
        @DecimalMin(value = "1.0", message = "O indice pluviometrico deve ser maior ou igual a 1.0.")
        Double indicePluviometrico,

        /** Usado apenas por SECO. */
        Boolean emEstacaoSeca,

        /** Obrigatório para UMIDO_MONITORADO; ignorado nos demais. */
        @Size(max = 50, message = "O id do sensor deve ter no maximo 50 caracteres.")
        String idSensor,

        /** Opcional: um trecho pode existir sem equipe designada. */
        Long equipeResponsavelId
) {
}
