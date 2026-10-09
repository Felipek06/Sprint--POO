package br.com.motiva.exception;

/**
 * Lançada quando um recurso pedido por ID não existe no banco.
 *
 * O ManipuladorGlobalDeErros traduz esta exceção para 404 Not Found. É esse
 * par — exceção no Service, tradução no @RestControllerAdvice — que permite
 * ao Service continuar falando a língua do negócio ("esse trecho não
 * existe") sem precisar conhecer códigos HTTP.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Atalho para a mensagem mais comum do projeto.
     *
     * @param recurso nome do recurso, ex.: "Trecho"
     * @param id      identificador procurado
     */
    public static RecursoNaoEncontradoException de(String recurso, Long id) {
        return new RecursoNaoEncontradoException(
                recurso + " de id " + id + " nao foi encontrado.");
    }
}
