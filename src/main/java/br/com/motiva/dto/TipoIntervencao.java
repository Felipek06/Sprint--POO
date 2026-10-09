package br.com.motiva.dto;

/**
 * Tipos de intervenção aceitos no corpo das requisições.
 *
 * Espelha os @DiscriminatorValue de RocadaMecanizada e Pulverizacao e os
 * valores aceitos pela constraint CK_INTERV_TIPO da Sprint 3.
 */
public enum TipoIntervencao {
    ROCADA_MECANIZADA,
    PULVERIZACAO
}
