package br.com.motiva.controller;

import br.com.motiva.dto.EquipeRequest;
import br.com.motiva.dto.EquipeResponse;
import br.com.motiva.service.EquipeManutencaoService;
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
 * Endpoints REST das equipes de manutenção.
 *
 * Repare no que NÃO existe nesta classe: não há if de validação, não há
 * cálculo, não há consulta ao banco e não há uma linha de SQL. O Controller
 * faz exatamente três coisas — recebe a requisição, chama o Service e
 * escolhe o código HTTP da resposta. Todo o resto está uma camada abaixo.
 */
@RestController
@RequestMapping("/api/equipes")
public class EquipeManutencaoController {

    private final EquipeManutencaoService equipeService;

    public EquipeManutencaoController(EquipeManutencaoService equipeService) {
        this.equipeService = equipeService;
    }

    /**
     * GET /api/equipes
     * GET /api/equipes?nome=alpha
     * GET /api/equipes?efetivoMinimo=5
     *
     * @return 200 OK com a lista (vazia se nada casar com o filtro)
     */
    @GetMapping
    public ResponseEntity<List<EquipeResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer efetivoMinimo) {

        if (nome != null && !nome.isBlank()) {
            return ResponseEntity.ok(equipeService.buscarPorNome(nome));
        }
        if (efetivoMinimo != null) {
            return ResponseEntity.ok(equipeService.buscarPorEfetivoMinimo(efetivoMinimo));
        }
        return ResponseEntity.ok(equipeService.listarTodas());
    }

    /**
     * GET /api/equipes/{id}
     *
     * @return 200 OK, ou 404 Not Found se o id não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipeService.buscarPorId(id));
    }

    /**
     * POST /api/equipes
     *
     * @return 201 Created com o corpo criado e o cabeçalho Location
     *         apontando para o novo recurso; 400 Bad Request se o corpo for
     *         inválido
     */
    @PostMapping
    public ResponseEntity<EquipeResponse> criar(@Valid @RequestBody EquipeRequest requisicao) {
        EquipeResponse criada = equipeService.criar(requisicao);

        URI localizacao = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(criada);
    }

    /**
     * PUT /api/equipes/{id}
     *
     * @return 200 OK com o corpo atualizado; 404 se o id não existir
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipeResponse> atualizar(@PathVariable Long id,
                                                    @Valid @RequestBody EquipeRequest requisicao) {
        return ResponseEntity.ok(equipeService.atualizar(id, requisicao));
    }

    /**
     * DELETE /api/equipes/{id}
     *
     * @return 204 No Content; 404 se o id não existir; 400 se a equipe ainda
     *         estiver vinculada a trechos ou intervenções
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        equipeService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
