package br.com.motiva.dto;

/**
 * Tipos de trecho aceitos no corpo das requisições.
 *
 * Existe um enum aqui, e não apenas uma String livre, para que o próprio
 * Jackson recuse um tipo inexistente antes mesmo de a requisição chegar ao
 * Service — e para que os valores válidos fiquem documentados em um lugar
 * só. Os nomes são idênticos aos @DiscriminatorValue das entidades e aos
 * valores aceitos pela constraint CK_TRECHO_TIPO da Sprint 3.
 */
public enum TipoTrecho {
    UMIDO,
    SECO,
    UMIDO_MONITORADO
}
