package br.com.motiva.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corpo aceito em POST e PUT de /api/equipes.
 *
 * Por que um DTO em vez de receber a entidade direto?
 *
 *  - A entidade tem id gerado pelo banco. Se o cliente mandasse a entidade,
 *    ele poderia tentar escolher o próprio id.
 *  - O contrato da API passa a ser independente do mapeamento. Trocar um
 *    nome de coluna não quebra quem consome a API, e vice-versa.
 *  - As anotações de validação ficam no DTO, onde elas pertencem: elas
 *    descrevem o formato da requisição, não uma regra do domínio.
 */
public record EquipeRequest(

        @NotBlank(message = "O nome da equipe e obrigatorio e nao pode ser vazio.")
        @Size(max = 100, message = "O nome da equipe deve ter no maximo 100 caracteres.")
        String nome,

        @NotNull(message = "A quantidade de integrantes e obrigatoria.")
        @Positive(message = "A equipe deve ter ao menos 1 integrante.")
        @Max(value = 999, message = "A quantidade de integrantes deve caber em 3 digitos.")
        Integer quantidadeIntegrantes
) {
}
