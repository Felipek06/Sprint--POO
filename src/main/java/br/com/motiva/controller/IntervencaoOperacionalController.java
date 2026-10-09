package br.com.motiva.controller;

import br.com.motiva.dto.IntervencaoRequest;
import br.com.motiva.dto.IntervencaoResponse;
import br.com.motiva.dto.TipoIntervencao;
import br.com.motiva.service.IntervencaoOperacionalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST das intervenções operacionais.
 */
@RestController
@RequestMapping("/api/intervencoes")
public class IntervencaoOperacionalController {

    private final IntervencaoOperacionalService intervencaoService;

    public IntervencaoOperacionalController(IntervencaoOperacionalService intervencaoService) {
        this.intervencaoService = intervencaoService;
    }

    /**
     * GET /api/intervencoes
     * GET /api/intervencoes?tipo=PULVERIZACAO
     * GET /api/intervencoes?trechoId=3
     *
     * @return 200 OK com a lista
     */
    @GetMapping
    public ResponseEntity<List<IntervencaoResponse>> listar(
            @RequestParam(required = false) TipoIntervencao tipo,
            @RequestParam(required = false) Long trechoId) {

        if (trechoId != null) {
            return ResponseEntity.ok(intervencaoService.listarPorTrecho(trechoId));
        }
        return ResponseEntity.ok(intervencaoService.listar(tipo));
    }

    /**
     * GET /api/intervencoes/{id}
     *
     * @return 200 OK, ou 404 Not Found se o id não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<IntervencaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(intervencaoService.buscarPorId(id));
    }

    /**
     * POST /api/intervencoes
     *
     * Registra e executa a intervenção. A resposta traz o campo
     * resultadoExecucao, que é o retorno polimórfico de executarServico().
     *
     * @return 201 Created com o corpo criado e o cabeçalho Location;
     *         404 se o trecho ou a equipe não existirem;
     *         400 se a intervenção não for permitida para aquele trecho
     */
    @PostMapping
    public ResponseEntity<IntervencaoResponse> registrar(
            @Valid @RequestBody IntervencaoRequest requisicao) {

        IntervencaoResponse registrada = intervencaoService.registrar(requisicao);

        URI localizacao = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registrada.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(registrada);
    }

    /**
     * PUT /api/intervencoes/{id}
     *
     * @return 200 OK com o corpo atualizado; 404 se o id não existir
     */
    @PutMapping("/{id}")
    public ResponseEntity<IntervencaoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody IntervencaoRequest requisicao) {

        return ResponseEntity.ok(intervencaoService.atualizar(id, requisicao));
    }

    /**
     * DELETE /api/intervencoes/{id}
     *
     * @return 204 No Content; 404 se o id não existir
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        intervencaoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
