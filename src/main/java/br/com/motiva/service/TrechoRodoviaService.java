package br.com.motiva.service;

import br.com.motiva.dto.TipoTrecho;
import br.com.motiva.dto.TrechoRequest;
import br.com.motiva.dto.TrechoResponse;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraDeNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.model.TrechoSeco;
import br.com.motiva.model.TrechoUmido;
import br.com.motiva.model.TrechoUmidoMonitorado;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio dos trechos de rodovia.
 *
 * É aqui que moram as validações que a Sprint 1 colocava nos construtores
 * de TrechoRodovia, TrechoUmido e TrechoSeco, e é aqui que a hierarquia da
 * Sprint 2 é instanciada a partir do tipo pedido na requisição.
 *
 * Por que a validação "nivelVegetacao >= 0" ficou neste Service e não no
 * Controller? Porque ela é uma regra do MOTIVA, não um detalhe do HTTP. Se
 * amanhã o sistema ganhar um importador de CSV, um job agendado ou voltar a
 * ter um menu de console, todos eles vão chamar este Service e herdar a
 * mesma validação. Se ela estivesse no Controller, valeria só para quem
 * entrasse pela porta do HTTP, e cada nova porta teria que reimplementá-la.
 */
@Service
@Transactional(readOnly = true)
public class TrechoRodoviaService {

    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoOperacionalRepository intervencaoRepository;
    private final EquipeManutencaoService equipeService;

    public TrechoRodoviaService(TrechoRodoviaRepository trechoRepository,
                                IntervencaoOperacionalRepository intervencaoRepository,
                                EquipeManutencaoService equipeService) {
        this.trechoRepository = trechoRepository;
        this.intervencaoRepository = intervencaoRepository;
        this.equipeService = equipeService;
    }

    /**
     * Lista os trechos, opcionalmente filtrando por tipo e/ou por nível
     * mínimo de vegetação. Cada filtro cai em uma derived query diferente
     * do repositório — nenhuma linha de SQL escrita à mão.
     */
    public List<TrechoResponse> listar(TipoTrecho tipo, Double nivelMinimoCm) {
        List<TrechoRodovia> trechos;

        if (tipo != null && nivelMinimoCm != null) {
            trechos = trechoRepository.findByTipo(tipo.name())
                    .stream()
                    .filter(trecho -> trecho.getNivelVegetacaoCm() >= nivelMinimoCm)
                    .toList();
        } else if (tipo != null) {
            trechos = trechoRepository.findByTipo(tipo.name());
        } else if (nivelMinimoCm != null) {
            trechos = trechoRepository.findByNivelVegetacaoCmGreaterThanEqual(nivelMinimoCm);
        } else {
            trechos = trechoRepository.findAll();
        }

        return trechos.stream().map(TrechoResponse::de).toList();
    }

    /** Trechos que ainda não têm equipe designada. */
    public List<TrechoResponse> listarSemEquipe() {
        return trechoRepository.findByEquipeResponsavelIsNull()
                .stream()
                .map(TrechoResponse::de)
                .toList();
    }

    public TrechoResponse buscarPorId(Long id) {
        return TrechoResponse.de(obterOuFalhar(id));
    }

    @Transactional
    public TrechoResponse criar(TrechoRequest requisicao) {
        validarQuilometragem(requisicao);
        validarCamposDoTipo(requisicao);

        if (trechoRepository.existsByQuilometroInicialAndQuilometroFinal(
                requisicao.quilometroInicial(), requisicao.quilometroFinal())) {
            throw new RegraDeNegocioException(
                    "Ja existe um trecho cadastrado cobrindo o KM "
                            + requisicao.quilometroInicial() + " ao KM " + requisicao.quilometroFinal() + ".");
        }

        TrechoRodovia trecho = instanciarPeloTipo(requisicao);
        associarEquipeSeInformada(trecho, requisicao.equipeResponsavelId());

        return TrechoResponse.de(trechoRepository.save(trecho));
    }

    @Transactional
    public TrechoResponse atualizar(Long id, TrechoRequest requisicao) {
        TrechoRodovia trecho = obterOuFalhar(id);

        /*
         * O tipo do trecho é o discriminador da linha. Trocá-lo significaria
         * transformar um objeto em outro de classe diferente, algo que o JPA
         * não faz num UPDATE. Para mudar de tipo, apague e crie de novo.
         */
        if (!trecho.getTipo().equals(requisicao.tipo().name())) {
            throw new RegraDeNegocioException(
                    "Nao e possivel alterar o tipo de um trecho ja cadastrado (atual: "
                            + trecho.getTipo() + ", solicitado: " + requisicao.tipo().name()
                            + "). Remova o trecho e cadastre outro.");
        }

        validarQuilometragem(requisicao);
        validarCamposDoTipo(requisicao);

        trecho.setQuilometroInicial(requisicao.quilometroInicial());
        trecho.setQuilometroFinal(requisicao.quilometroFinal());
        trecho.setNivelVegetacaoCm(requisicao.nivelVegetacaoCm());

        if (trecho instanceof TrechoUmido umido) {
            umido.setIndicePluviometrico(
                    requisicao.indicePluviometrico() == null ? 1.0 : requisicao.indicePluviometrico());
        }
        if (trecho instanceof TrechoSeco seco) {
            seco.setEmEstacaoSeca(Boolean.TRUE.equals(requisicao.emEstacaoSeca()));
        }
        if (trecho instanceof TrechoUmidoMonitorado monitorado) {
            monitorado.setIdSensor(requisicao.idSensor().trim());
        }

        if (requisicao.equipeResponsavelId() == null) {
            trecho.desassociarEquipe();
        } else {
            associarEquipeSeInformada(trecho, requisicao.equipeResponsavelId());
        }

        return TrechoResponse.de(trecho);
    }

    @Transactional
    public void remover(Long id) {
        TrechoRodovia trecho = obterOuFalhar(id);

        if (intervencaoRepository.existsByTrechoAlvoId(id)) {
            throw new RegraDeNegocioException(
                    "O trecho de id " + id + " nao pode ser removido: "
                            + "existem intervencoes registradas para ele. "
                            + "Remova as intervencoes primeiro.");
        }

        trechoRepository.delete(trecho);
    }

    /**
     * Roda o motor de crescimento da Sprint 2 sobre um trecho.
     *
     * Este endpoint existe para tornar visível, pela API, o polimorfismo que
     * antes só aparecia no console: o mesmo simularCrescimento(dias) produz
     * resultados diferentes conforme a subclasse, porque cada uma tem a sua
     * calcularTaxaCrescimentoDiario().
     */
    @Transactional
    public TrechoResponse simularCrescimento(Long id, int dias) {
        if (dias < 0) {
            throw new RegraDeNegocioException("O numero de dias nao pode ser negativo.");
        }

        TrechoRodovia trecho = obterOuFalhar(id);
        trecho.simularCrescimento(dias);

        return TrechoResponse.de(trecho);
    }

    // -------------------------------------------------------------------------
    // Apoio interno
    // -------------------------------------------------------------------------

    /** Carrega a entidade gerenciada ou lança 404. */
    TrechoRodovia obterOuFalhar(Long id) {
        return trechoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Trecho", id));
    }

    /**
     * Instancia a subclasse correta a partir do tipo pedido.
     *
     * Este switch é o único lugar do projeto que decide qual subclasse criar,
     * e ele existe porque alguém precisa traduzir um texto vindo de fora
     * (o JSON) em um objeto Java. Na LEITURA do banco, por outro lado, não
     * existe switch nenhum: quem faz essa escolha é o Hibernate, a partir do
     * @DiscriminatorValue.
     */
    private TrechoRodovia instanciarPeloTipo(TrechoRequest requisicao) {
        double indicePluviometrico = requisicao.indicePluviometrico() == null
                ? 1.0
                : requisicao.indicePluviometrico();

        return switch (requisicao.tipo()) {
            case UMIDO -> new TrechoUmido(
                    requisicao.quilometroInicial(),
                    requisicao.quilometroFinal(),
                    requisicao.nivelVegetacaoCm(),
                    indicePluviometrico);

            case SECO -> new TrechoSeco(
                    requisicao.quilometroInicial(),
                    requisicao.quilometroFinal(),
                    requisicao.nivelVegetacaoCm(),
                    Boolean.TRUE.equals(requisicao.emEstacaoSeca()));

            case UMIDO_MONITORADO -> new TrechoUmidoMonitorado(
                    requisicao.quilometroInicial(),
                    requisicao.quilometroFinal(),
                    requisicao.nivelVegetacaoCm(),
                    indicePluviometrico,
                    requisicao.idSensor().trim());
        };
    }

    /** Regras que comparam dois campos entre si — fora do alcance do Bean Validation. */
    private void validarQuilometragem(TrechoRequest requisicao) {
        if (requisicao.quilometroFinal() <= requisicao.quilometroInicial()) {
            throw new RegraDeNegocioException(
                    "O quilometro final (" + requisicao.quilometroFinal()
                            + ") deve ser maior que o inicial (" + requisicao.quilometroInicial() + ").");
        }
    }

    /** Regras de obrigatoriedade que dependem do tipo escolhido. */
    private void validarCamposDoTipo(TrechoRequest requisicao) {
        if (requisicao.tipo() == TipoTrecho.UMIDO_MONITORADO
                && (requisicao.idSensor() == null || requisicao.idSensor().isBlank())) {
            throw new RegraDeNegocioException(
                    "Trechos do tipo UMIDO_MONITORADO exigem o campo 'idSensor'.");
        }
    }

    private void associarEquipeSeInformada(TrechoRodovia trecho, Long idEquipe) {
        if (idEquipe == null) {
            return;
        }
        EquipeManutencao equipe = equipeService.obterOuFalhar(idEquipe);
        trecho.associarEquipe(equipe);
    }
}
