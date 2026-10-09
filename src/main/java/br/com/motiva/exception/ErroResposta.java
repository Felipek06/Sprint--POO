package br.com.motiva.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Corpo JSON padronizado de todas as respostas de erro da API.
 *
 * Ter um formato único de erro é o que permite a um cliente (Postman, front,
 * outro serviço) tratar falhas de forma genérica, sem adivinhar o formato
 * caso a caso.
 *
 * @param timestamp  instante em que o erro ocorreu
 * @param status     código HTTP numérico, ex.: 404
 * @param erro       nome do código HTTP, ex.: "Not Found"
 * @param mensagem   explicação legível do que deu errado
 * @param caminho    rota que foi chamada
 * @param campos     erros por campo, presente apenas em falhas de validação
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, List<String>> campos
) {

    public static ErroResposta de(int status, String erro, String mensagem, String caminho) {
        return new ErroResposta(LocalDateTime.now(), status, erro, mensagem, caminho, null);
    }

    public static ErroResposta deValidacao(int status, String erro, String mensagem,
                                           String caminho, Map<String, List<String>> campos) {
        return new ErroResposta(LocalDateTime.now(), status, erro, mensagem, caminho, campos);
    }
}
