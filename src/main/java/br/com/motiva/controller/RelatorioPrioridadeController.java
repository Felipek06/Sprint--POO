package br.com.motiva.controller;

import br.com.motiva.dto.RelatorioGeradoResponse;
import br.com.motiva.dto.RelatorioResponse;
import br.com.motiva.service.RelatorioPrioridadeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints REST do motor de prioridade (item 3.3 do enunciado).
 *
 * Esta classe é a prova de que a regra de negócio ficou no Service: não há
 * aqui nenhum número 80, 50 ou 25, nenhuma comparação de nível e nenhuma
 * decisão sobre o que é urgente. O Controller só expõe o motor pela rede.
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioPrioridadeController {

    private final RelatorioPrioridadeService relatorioService;

    public RelatorioPrioridadeController(RelatorioPrioridadeService relatorioService) {
        this.relatorioService = relatorioService;
    }

    /**
     * POST /api/relatorios
     *
     * Gera o relatório a partir dos trechos atuais e persiste o snapshot no
     * histórico.
     *
     * @return 201 Created com o relatório gerado, o detalhamento trecho a
     *         trecho e o cabeçalho Location apontando para o snapshot;
     *         400 Bad Request se não houver trechos cadastrados
     */
    @PostMapping
    public ResponseEntity<RelatorioGeradoResponse> gerar() {
        RelatorioGeradoResponse gerado = relatorioService.gerar();

        URI localizacao = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(gerado.relatorio().id())
                .toUri();

        return ResponseEntity.created(localizacao).body(gerado);
    }

    /**
     * GET /api/relatorios
     *
     * @return 200 OK com o histórico, do mais recente para o mais antigo
     */
    @GetMapping
    public ResponseEntity<List<RelatorioResponse>> listarHistorico() {
        return ResponseEntity.ok(relatorioService.listarHistorico());
    }

    /**
     * GET /api/relatorios/periodo?inicio=2026-10-01T00:00:00&fim=2026-10-31T23:59:59
     *
     * As datas vêm no formato ISO-8601. O @DateTimeFormat(ISO.DATE_TIME) é
     * o que ensina o Spring a converter a string da query em LocalDateTime.
     *
     * @return 200 OK com os relatórios do período;
     *         400 Bad Request se a data inicial for posterior à final, ou se
     *         algum parâmetro faltar
     */
    @GetMapping("/periodo")
    public ResponseEntity<List<RelatorioResponse>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        return ResponseEntity.ok(relatorioService.buscarPorPeriodo(inicio, fim));
    }

    /**
     * GET /api/relatorios/{id}
     *
     * @return 200 OK, ou 404 Not Found se o id não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<RelatorioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(relatorioService.buscarPorId(id));
    }
}
