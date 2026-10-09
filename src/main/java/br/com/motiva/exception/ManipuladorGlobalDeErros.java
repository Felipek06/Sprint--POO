package br.com.motiva.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tratamento global de erros da API (item de bônus do enunciado).
 *
 * Sem esta classe, cada Controller precisaria repetir try/catch para
 * devolver 404 e 400 — e qualquer exceção esquecida vazaria como um 500
 * com stack trace no corpo da resposta.
 *
 * Com ela, os Controllers ficam com o caminho feliz apenas: lançar a
 * exceção certa é responsabilidade do Service, traduzi-la para HTTP é
 * responsabilidade daqui. É a mesma separação de camadas pedida no item
 * 3.1, aplicada ao tratamento de falhas.
 */
@RestControllerAdvice
public class ManipuladorGlobalDeErros {

    private static final Logger log = LoggerFactory.getLogger(ManipuladorGlobalDeErros.class);

    /**
     * Recurso inexistente -> 404 Not Found.
     * Usado por GET, PUT e DELETE em /{id}.
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErroResposta.de(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage(),
                        requisicao.getRequestURI()));
    }

    /**
     * Violação de regra de negócio -> 400 Bad Request.
     * A requisição está bem formada, mas pede algo que o domínio não aceita.
     */
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RegraDeNegocioException ex,
                                                              HttpServletRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErroResposta.de(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        ex.getMessage(),
                        requisicao.getRequestURI()));
    }

    /**
     * Falha do Bean Validation (@Valid no corpo da requisição) -> 400.
     *
     * Aqui o corpo da resposta traz o mapa "campos", dizendo exatamente qual
     * atributo do JSON foi recusado e por quê.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex,
                                                         HttpServletRequest requisicao) {
        Map<String, List<String>> campos = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(erro ->
                campos.computeIfAbsent(erro.getField(), chave -> new ArrayList<>())
                        .add(erro.getDefaultMessage()));

        ex.getBindingResult().getGlobalErrors().forEach(erro ->
                campos.computeIfAbsent(erro.getObjectName(), chave -> new ArrayList<>())
                        .add(erro.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErroResposta.deValidacao(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Um ou mais campos da requisicao sao invalidos.",
                        requisicao.getRequestURI(),
                        campos));
    }

    /** Invariante de domínio violada dentro de uma entidade -> 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> tratarArgumentoIlegal(IllegalArgumentException ex,
                                                               HttpServletRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErroResposta.de(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        ex.getMessage(),
                        requisicao.getRequestURI()));
    }

    /**
     * JSON malformado, ou valor que não cabe no tipo esperado (por exemplo,
     * um tipoProduto que não existe no enum) -> 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(HttpMessageNotReadableException ex,
                                                            HttpServletRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErroResposta.de(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Corpo da requisicao ilegivel ou com valor fora do dominio esperado. "
                                + "Verifique o JSON enviado.",
                        requisicao.getRequestURI()));
    }

    /** Parâmetro de rota ou de query com tipo errado (ex.: /api/trechos/abc) -> 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErroResposta.de(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "O parametro '" + ex.getName() + "' recebeu um valor invalido: " + ex.getValue(),
                        requisicao.getRequestURI()));
    }

    /** Parâmetro obrigatório ausente (ex.: /api/relatorios/periodo sem inicio) -> 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResposta> tratarParametroAusente(MissingServletRequestParameterException ex,
                                                                HttpServletRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErroResposta.de(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "O parametro obrigatorio '" + ex.getParameterName() + "' nao foi informado.",
                        requisicao.getRequestURI()));
    }

    /**
     * Constraint do banco violada (FK, UNIQUE, CHECK) -> 409 Conflict.
     *
     * Normalmente o Service já barra esses casos antes, com mensagem melhor.
     * Este handler é a rede de segurança para o que escapar.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException ex,
                                                           HttpServletRequest requisicao) {
        log.warn("Violacao de integridade no banco: {}", ex.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErroResposta.de(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        "A operacao viola uma restricao de integridade do banco de dados.",
                        requisicao.getRequestURI()));
    }

    /**
     * Rede de segurança final -> 500.
     *
     * O stack trace vai para o log do servidor, não para o cliente: expor
     * detalhes internos numa resposta HTTP é falha de segurança.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception ex,
                                                              HttpServletRequest requisicao) {
        log.error("Erro nao tratado em {}", requisicao.getRequestURI(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResposta.de(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Erro interno inesperado. Consulte o log do servidor.",
                        requisicao.getRequestURI()));
    }
}
