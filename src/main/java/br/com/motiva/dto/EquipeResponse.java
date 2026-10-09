package br.com.motiva.dto;

import br.com.motiva.model.EquipeManutencao;

/**
 * Representação de uma equipe no corpo das respostas da API.
 */
public record EquipeResponse(
        Long id,
        String nome,
        Integer quantidadeIntegrantes
) {

    public static EquipeResponse de(EquipeManutencao equipe) {
        if (equipe == null) {
            return null;
        }
        return new EquipeResponse(
                equipe.getId(),
                equipe.getNome(),
                equipe.getQuantidadeIntegrantes());
    }
}
