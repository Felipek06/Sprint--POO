package br.com.motiva.controller;

import br.com.motiva.dto.TipoTrecho;
import br.com.motiva.dto.TrechoRequest;
import br.com.motiva.dto.TrechoResponse;
import br.com.motiva.service.TrechoRodoviaService;
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
 * Endpoints REST dos trechos de rodovia.
 *
 * Esta é a entidade central do MOTIVA, e é por aqui que a herança da
 * Sprint 2 aparece na API: um único conjunto de rotas atende os três tipos
 * de trecho, e o campo "tipo" do corpo decide qual subclasse será criada.
 */
@RestController
@RequestMapping("/api/trechos")
public class TrechoRodoviaController {

    private final TrechoRodoviaService trechoService;

    public TrechoRodoviaController(TrechoRodoviaService trechoService) {
        this.trechoService = trechoService;
    }

    /**
     * GET /api/trechos
     * GET /api/trechos?tipo=UMIDO
     * GET /api/trechos?nivelMinimoCm=50
     * GET /api/trechos?semEquipe=true
     *
     * Os filtros tipo e nivelMinimoCm são servidos pelas duas derived
     * queries exigidas no item 3.4 do enunciado.
     *
     * @return 200 OK com a lista
     */
    @GetMapping
    public ResponseEntity<List<TrechoResponse>> listar(
            @RequestParam(required = false) TipoTrecho tipo,
            @RequestParam(required = false) Double nivelMinimoCm,
            @RequestParam(required = false, defaultValue = "false") boolean semEquipe) {

        if (semEquipe) {
            return ResponseEntity.ok(trechoService.listarSemEquipe());
        }
        return ResponseEntity.ok(trechoService.listar(tipo, nivelMinimoCm));
    }

    /**
     * GET /api/trechos/{id}
     *
     * @return 200 OK, ou 404 Not Found se o id não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrechoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(trechoService.buscarPorId(id));
    }

    /**
     * POST /api/trechos
     *
     * @return 201 Created com o corpo criado e o cabeçalho Location;
     *         400 Bad Request se o corpo violar formato ou regra de negócio
     */
    @PostMapping
    public ResponseEntity<TrechoResponse> criar(@Valid @RequestBody TrechoRequest requisicao) {
        TrechoResponse criado = trechoService.criar(requisicao);

        URI localizacao = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(criado);
    }

    /**
     * PUT /api/trechos/{id}
     *
     * @return 200 OK com o corpo atualizado; 404 se o id não existir
     */
    @PutMapping("/{id}")
    public ResponseEntity<TrechoResponse> atualizar(@PathVariable Long id,
                                                    @Valid @RequestBody TrechoRequest requisicao) {
        return ResponseEntity.ok(trechoService.atualizar(id, requisicao));
    }

    /**
     * POST /api/trechos/{id}/simulacao?dias=30
     *
     * Roda o motor de crescimento da Sprint 2 sobre o trecho: cada subclasse
     * cresce a uma taxa diferente, e o nível resultante é persistido.
     *
     * @return 200 OK com o trecho já atualizado
     */
    @PostMapping("/{id}/simulacao")
    public ResponseEntity<TrechoResponse> simularCrescimento(@PathVariable Long id,
                                                             @RequestParam int dias) {
        return ResponseEntity.ok(trechoService.simularCrescimento(id, dias));
    }

    /**
     * DELETE /api/trechos/{id}
     *
     * @return 204 No Content; 404 se o id não existir; 400 se o trecho ainda
     *         tiver intervenções registradas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        trechoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
