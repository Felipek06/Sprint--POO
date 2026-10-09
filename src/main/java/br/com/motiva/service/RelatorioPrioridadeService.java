package br.com.motiva.service;

import br.com.motiva.dto.RelatorioGeradoResponse;
import br.com.motiva.dto.RelatorioResponse;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraDeNegocioException;
import br.com.motiva.model.MonitoravelViaIoT;
import br.com.motiva.model.Prioridade;
import br.com.motiva.model.RelatorioPrioridade;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.RelatorioPrioridadeRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de prioridade de roçada — o coração do MOTIVA.
 *
 * Este é o GeradorRelatorio das Sprints 2 e 3, migrado para a camada
 * @Service. A lógica de classificação é a mesma de sempre:
 *
 *   nível >= 80 cm  -> URGENTE  -> Roçada Mecanizada
 *   nível >= 50 cm  -> CRÍTICO  -> Pulverização
 *   nível >= 25 cm  -> ATENÇÃO  -> Agendar intervenção
 *   nível <  25 cm  -> NORMAL   -> Sem ação
 *
 * O que mudou foi o entorno, não a regra:
 *
 *  - os trechos não vêm mais de um array montado na mão no Main, e sim do
 *    repositório;
 *  - o resultado não é mais impresso com System.out.println, e sim
 *    devolvido como objeto para o Controller transformar em JSON;
 *  - o histórico é salvo com um save(), não com um INSERT no DAO;
 *  - os limiares continuam fora do Controller, como exige o item 3.3 do
 *    enunciado.
 */
@Service
@Transactional(readOnly = true)
public class RelatorioPrioridadeService {

    private final RelatorioPrioridadeRepository relatorioRepository;
    private final TrechoRodoviaRepository trechoRepository;

    public RelatorioPrioridadeService(RelatorioPrioridadeRepository relatorioRepository,
                                      TrechoRodoviaRepository trechoRepository) {
        this.relatorioRepository = relatorioRepository;
        this.trechoRepository = trechoRepository;
    }

    /**
     * Gera o relatório a partir dos trechos atuais e persiste o snapshot no
     * histórico (POST /api/relatorios).
     */
    @Transactional
    public RelatorioGeradoResponse gerar() {
        List<TrechoRodovia> trechos = trechoRepository.findAll();

        if (trechos.isEmpty()) {
            throw new RegraDeNegocioException(
                    "Nao ha trechos cadastrados para gerar o relatorio de prioridade.");
        }

        List<RelatorioGeradoResponse.ItemClassificado> itens = new ArrayList<>();
        Map<Prioridade, Integer> contagem = new EnumMap<>(Prioridade.class);
        for (Prioridade prioridade : Prioridade.values()) {
            contagem.put(prioridade, 0);
        }

        for (TrechoRodovia trecho : trechos) {
            Double leituraSensor = atualizarComSensorSeMonitorado(trecho);

            Prioridade prioridade = trecho.classificarPrioridade();
            contagem.merge(prioridade, 1, Integer::sum);

            itens.add(new RelatorioGeradoResponse.ItemClassificado(
                    trecho.getId(),
                    trecho.getDescricaoTipo(),
                    trecho.getQuilometroInicial(),
                    trecho.getQuilometroFinal(),
                    trecho.getNivelVegetacaoCm(),
                    leituraSensor,
                    prioridade.name(),
                    prioridade.getRecomendacao()));
        }

        String resumo = String.format(
                "%d trecho(s) analisado(s): %d urgente(s), %d critico(s), %d em atencao, %d normal(is).",
                trechos.size(),
                contagem.get(Prioridade.URGENTE),
                contagem.get(Prioridade.CRITICO),
                contagem.get(Prioridade.ATENCAO),
                contagem.get(Prioridade.NORMAL));

        RelatorioPrioridade snapshot = relatorioRepository.save(new RelatorioPrioridade(
                contagem.get(Prioridade.URGENTE),
                contagem.get(Prioridade.CRITICO),
                contagem.get(Prioridade.ATENCAO),
                contagem.get(Prioridade.NORMAL),
                resumo));

        return new RelatorioGeradoResponse(RelatorioResponse.de(snapshot), itens);
    }

    /** Histórico completo, do mais recente para o mais antigo. */
    public List<RelatorioResponse> listarHistorico() {
        return relatorioRepository.findAllByOrderByDataGeracaoDesc()
                .stream()
                .map(RelatorioResponse::de)
                .toList();
    }

    /**
     * Histórico dentro de um período
     * (GET /api/relatorios/periodo?inicio=...&fim=...).
     */
    public List<RelatorioResponse> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio.isAfter(fim)) {
            throw new RegraDeNegocioException(
                    "A data inicial nao pode ser posterior a data final.");
        }

        return relatorioRepository.findByDataGeracaoBetweenOrderByDataGeracaoDesc(inicio, fim)
                .stream()
                .map(RelatorioResponse::de)
                .toList();
    }

    public RelatorioResponse buscarPorId(Long id) {
        return relatorioRepository.findById(id)
                .map(RelatorioResponse::de)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Relatorio", id));
    }

    // -------------------------------------------------------------------------
    // Apoio interno
    // -------------------------------------------------------------------------

    /**
     * Para trechos com sensor IoT, consulta a leitura antes de classificar.
     *
     * O teste é feito contra a INTERFACE MonitoravelViaIoT, não contra uma
     * classe concreta. Se amanhã surgir um TrechoUrbanoMonitorado, ele entra
     * nesta rotina sem que uma linha daqui precise mudar — é o ganho de
     * programar contra contrato, apresentado na Sprint 2.
     *
     * Quando a leitura indica vegetação maior que a registrada, a diferença
     * é incorporada ao trecho. Como estamos dentro de uma transação, esse
     * ajuste é gravado no banco pelo dirty checking do Hibernate.
     *
     * @return a leitura do sensor, ou null se o trecho não for monitorado
     */
    private Double atualizarComSensorSeMonitorado(TrechoRodovia trecho) {
        if (!(trecho instanceof MonitoravelViaIoT sensor)) {
            return null;
        }

        double leitura = sensor.transmitirDadosSensor();
        double diferenca = leitura - trecho.getNivelVegetacaoCm();

        if (diferenca > 0) {
            trecho.registrarCrescimento(diferenca);
        }

        return leitura;
    }
}
