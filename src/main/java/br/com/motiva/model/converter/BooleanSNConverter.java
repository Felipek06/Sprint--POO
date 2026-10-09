package br.com.motiva.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converte o boolean do Java para o CHAR(1) 'S'/'N' usado na coluna
 * EM_ESTACAO_SECA da tabela TRECHO_RODOVIA (modelada na Sprint 3).
 *
 * E aqui que aparece uma vantagem concreta do JPA sobre o JDBC puro: na
 * Sprint 3 essa traducao estava espalhada dentro do TrechoRodoviaDAO, em
 * cada setString/getString. Agora ela existe em UM lugar so, e o Hibernate
 * a aplica sozinho em todo INSERT, UPDATE e SELECT da coluna.
 */
@Converter
public class BooleanSNConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        if (valor == null) {
            return null;
        }
        return valor ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String coluna) {
        if (coluna == null || coluna.isBlank()) {
            return null;
        }
        return "S".equalsIgnoreCase(coluna.trim());
    }
}
