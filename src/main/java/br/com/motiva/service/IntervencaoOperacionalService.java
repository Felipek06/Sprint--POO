package br.com.motiva.service;

import br.com.motiva.dto.IntervencaoRequest;
import br.com.motiva.dto.IntervencaoResponse;
import br.com.motiva.dto.TipoIntervencao;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraDeNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.Prioridade;
import br.com.motiva.model.Pulverizacao;
import br.com.motiva.model.RocadaMecanizada;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio das intervenções operacionais.
 *
 * Este Service concentra o que a Sprint 2 distribuía entre o Main e as
 * subclasses de IntervencaoOperacional: decidir se a intervenção pedida faz
 * sentido, montar o objeto certo e disparar a execução polimórfica.
 */
@Service
@Transactional(readOnly = true)
public class IntervencaoOperacionalService {

    private final IntervencaoOperacionalRepository intervencaoRepository;
    private final TrechoRodoviaService trechoService;
    private final EquipeManutencaoService equipeService;

    public IntervencaoOperacionalService(IntervencaoOperacionalRepository intervencaoRepository,
                                         TrechoRodoviaService trechoService,
                                         EquipeManutencaoService equipeService) {
        this.intervencaoRepository = intervencaoRepository;
        this.trechoService = trechoService;
        this.equipeService = equipeService;
    }

    public List<IntervencaoResponse> listar(TipoIntervencao tipo) {
        List<IntervencaoOperacional> intervencoes = (tipo == null)
                ? intervencaoRepository.findAll()
                : intervencaoRepository.findByTipo(tipo.name());

        return intervencoes.stream().map(IntervencaoResponse::de).toList();
    }

    /** Histórico de intervenções de um trecho, da mais recente para a mais antiga. */
    public List<IntervencaoResponse> listarPorTrecho(Long idTrecho) {
        trechoService.obterOuFalhar(idTrecho);

        return intervencaoRepository.findByTrechoAlvoIdOrderByDataExecucaoDesc(idTrecho)
                .stream()
                .map(IntervencaoResponse::de)
                .toList();
    }

    public IntervencaoResponse buscarPorId(Long id) {
        return IntervencaoResponse.de(obterOuFalhar(id));
    }

    /**
     * Registra e executa uma intervenção.
     *
     * O ponto alto do método é a chamada a executarServico(): este Service
     * não sabe — e não precisa saber — se está lidando com uma roçada ou com
     * uma pulverização. Cada subclasse aplica o seu próprio efeito sobre o
     * trecho e devolve a descrição do que fez. É o polimorfismo da Sprint 2,
     * agora produzindo uma resposta HTTP em vez de texto no console.
     */
    @Transactional
    public IntervencaoResponse registrar(IntervencaoRequest requisicao) {
        TrechoRodovia trecho = trechoService.obterOuFalhar(requisicao.trechoAlvoId());
        EquipeManutencao equipe = equipeService.obterOuFalhar(requisicao.equipeResponsavelId());

        validarProdutoCompativelComTipo(requisicao);
        validarTrechoJustificaIntervencao(trecho);

        IntervencaoOperacional intervencao = switch (requisicao.tipo()) {
            case ROCADA_MECANIZADA -> new RocadaMecanizada(trecho, equipe);
            case PULVERIZACAO -> new Pulverizacao(trecho, equipe, requisicao.tipoProduto());
        };

        /*
         * Chamada polimórfica: a roçada baixa o nível do trecho para 20 cm,
         * a pulverização não mexe no nível. O trecho é uma entidade
         * gerenciada, então a alteração vira UPDATE automaticamente ao final
         * da transação, sem nenhum SQL escrito aqui.
         */
        String resultado = intervencao.executarServico();

        IntervencaoOperacional salva = intervencaoRepository.save(intervencao);

        // Designa a equipe que executou como responsável pelo trecho.
        trecho.associarEquipe(equipe);

        return IntervencaoResponse.de(salva, resultado);
    }

    /**
     * Atualiza os dados cadastrais de uma intervenção já registrada.
     *
     * O efeito sobre o trecho não é reaplicado: a intervenção já aconteceu
     * no mundo real, e reexecutá-la a cada PUT falsearia o nível de
     * vegetação. O que se corrige aqui é o registro, não o passado.
     */
    @Transactional
    public IntervencaoResponse atualizar(Long id, IntervencaoRequest requisicao) {
        IntervencaoOperacional intervencao = obterOuFalhar(id);

        if (!intervencao.getTipo().equals(requisicao.tipo().name())) {
            throw new RegraDeNegocioException(
                    "Nao e possivel alterar o tipo de uma intervencao ja registrada (atual: "
                            + intervencao.getTipo() + ", solicitado: " + requisicao.tipo().name()
                            + "). Remova o registro e crie outro.");
        }

        validarProdutoCompativelComTipo(requisicao);

        TrechoRodovia trecho = trechoService.obterOuFalhar(requisicao.trechoAlvoId());
        EquipeManutencao equipe = equipeService.obterOuFalhar(requisicao.equipeResponsavelId());

        intervencao.setTrechoAlvo(trecho);
        intervencao.setEquipeResponsavel(equipe);

        if (intervencao instanceof Pulverizacao pulverizacao) {
            pulverizacao.setTipoProduto(requisicao.tipoProduto());
        }

        return IntervencaoResponse.de(intervencao);
    }

    @Transactional
    public void remover(Long id) {
        intervencaoRepository.delete(obterOuFalhar(id));
    }

    // -------------------------------------------------------------------------
    // Apoio interno
    // -------------------------------------------------------------------------

    private IntervencaoOperacional obterOuFalhar(Long id) {
        return intervencaoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Intervencao", id));
    }

    /** O produto herbicida só faz sentido para pulverização. */
    private void validarProdutoCompativelComTipo(IntervencaoRequest requisicao) {
        if (requisicao.tipo() == TipoIntervencao.PULVERIZACAO && requisicao.tipoProduto() == null) {
            throw new RegraDeNegocioException(
                    "Intervencoes do tipo PULVERIZACAO exigem o campo 'tipoProduto'. "
                            + "Valores aceitos: HERBICIDA_SELETIVO, HERBICIDA_TOTAL, REGULADOR_CRESCIMENTO.");
        }
        if (requisicao.tipo() == TipoIntervencao.ROCADA_MECANIZADA && requisicao.tipoProduto() != null) {
            throw new RegraDeNegocioException(
                    "Intervencoes do tipo ROCADA_MECANIZADA nao usam produto quimico; "
                            + "remova o campo 'tipoProduto' da requisicao.");
        }
    }

    /**
     * Regra de negócio do MOTIVA: não se despacha equipe para um trecho
     * classificado como NORMAL (menos de 25 cm de vegetação).
     *
     * O objetivo do sistema é priorizar roçada onde ela é necessária;
     * autorizar uma intervenção em vegetação baixa desperdiça equipe e
     * contradiz o próprio relatório de prioridade.
     */
    private void validarTrechoJustificaIntervencao(TrechoRodovia trecho) {
        if (trecho.classificarPrioridade() == Prioridade.NORMAL) {
            throw new RegraDeNegocioException(String.format(
                    "O trecho de id %d esta com %.1f cm de vegetacao e foi classificado como NORMAL. "
                            + "Intervencoes so sao autorizadas a partir de %.0f cm (faixa ATENCAO).",
                    trecho.getId(),
                    trecho.getNivelVegetacaoCm(),
                    Prioridade.ATENCAO.getLimiteInferiorCm()));
        }
    }
}
