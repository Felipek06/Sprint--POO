package br.com.motiva.exception;

/**
 * Lançada quando a requisição está bem formada, mas viola uma regra de
 * negócio do MOTIVA — por exemplo, um quilômetro final menor que o inicial,
 * ou a tentativa de apagar um trecho que já tem intervenções registradas.
 *
 * O ManipuladorGlobalDeErros traduz esta exceção para 400 Bad Request.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
