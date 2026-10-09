package br.com.motiva.service;

import br.com.motiva.dto.EquipeRequest;
import br.com.motiva.dto.EquipeResponse;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraDeNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.repository.EquipeManutencaoRepository;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio das equipes de manutenção.
 *
 * As validações que estavam no construtor de EquipeManutencao na Sprint 1
 * (nome não vazio, pelo menos 1 integrante) vivem agora em duas camadas,
 * cada uma com um papel diferente:
 *
 *  - o formato é checado pelo Bean Validation no EquipeRequest;
 *  - as regras que precisam consultar o banco — nome duplicado, equipe com
 *    histórico — ficam aqui, porque só o Service enxerga os repositórios.
 */
@Service
@Transactional(readOnly = true)
public class EquipeManutencaoService {

    private final EquipeManutencaoRepository equipeRepository;
    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoOperacionalRepository intervencaoRepository;

    /**
     * Injeção por construtor.
     *
     * Na Sprint 3 cada classe criava seus próprios DAOs com new. Aqui o
     * Spring entrega as dependências prontas, e os campos podem ser final —
     * o que torna impossível existir um Service pela metade.
     */
    public EquipeManutencaoService(EquipeManutencaoRepository equipeRepository,
                                   TrechoRodoviaRepository trechoRepository,
                                   IntervencaoOperacionalRepository intervencaoRepository) {
        this.equipeRepository = equipeRepository;
        this.trechoRepository = trechoRepository;
        this.intervencaoRepository = intervencaoRepository;
    }

    public List<EquipeResponse> listarTodas() {
        return equipeRepository.findAll()
                .stream()
                .map(EquipeResponse::de)
                .toList();
    }

    /** Filtro por parte do nome, usando a derived query do repositório. */
    public List<EquipeResponse> buscarPorNome(String trechoDoNome) {
        return equipeRepository.findByNomeContainingIgnoreCase(trechoDoNome)
                .stream()
                .map(EquipeResponse::de)
                .toList();
    }

    /** Equipes com efetivo suficiente para uma determinada operação. */
    public List<EquipeResponse> buscarPorEfetivoMinimo(Integer minimo) {
        return equipeRepository.findByQuantidadeIntegrantesGreaterThanEqual(minimo)
                .stream()
                .map(EquipeResponse::de)
                .toList();
    }

    public EquipeResponse buscarPorId(Long id) {
        return EquipeResponse.de(obterOuFalhar(id));
    }

    @Transactional
    public EquipeResponse criar(EquipeRequest requisicao) {
        validarNomeDisponivel(requisicao.nome(), null);

        EquipeManutencao equipe = new EquipeManutencao(
                requisicao.nome().trim(),
                requisicao.quantidadeIntegrantes());

        // Uma linha. Na Sprint 3 isto eram ~40 linhas de JDBC no DAO.
        return EquipeResponse.de(equipeRepository.save(equipe));
    }

    @Transactional
    public EquipeResponse atualizar(Long id, EquipeRequest requisicao) {
        EquipeManutencao equipe = obterOuFalhar(id);
        validarNomeDisponivel(requisicao.nome(), id);

        equipe.setNome(requisicao.nome().trim());
        equipe.setQuantidadeIntegrantes(requisicao.quantidadeIntegrantes());

        /*
         * Não existe chamada a save() aqui, e isso é intencional: dentro de
         * uma transação o Hibernate acompanha a entidade carregada e, ao
         * fechar a transação, compara o estado atual com o original e emite
         * o UPDATE sozinho (dirty checking). É o oposto da Sprint 3, em que
         * era preciso montar o UPDATE campo a campo.
         */
        return EquipeResponse.de(equipe);
    }

    @Transactional
    public void remover(Long id) {
        EquipeManutencao equipe = obterOuFalhar(id);

        /*
         * As FKs ID_EQUIPE_RESPONSAVEL em TRECHO_RODOVIA e
         * INTERVENCAO_OPERACIONAL impediriam a exclusão de qualquer jeito,
         * mas o erro que o banco devolveria seria ilegível para quem está
         * consumindo a API. Checar antes permite responder 400 com uma
         * mensagem que explica o problema.
         */
        if (!trechoRepository.findByEquipeResponsavelId(id).isEmpty()) {
            throw new RegraDeNegocioException(
                    "A equipe '" + equipe.getNome() + "' nao pode ser removida: "
                            + "ela ainda e responsavel por trechos. Desassocie os trechos primeiro.");
        }
        if (intervencaoRepository.existsByEquipeResponsavelId(id)) {
            throw new RegraDeNegocioException(
                    "A equipe '" + equipe.getNome() + "' nao pode ser removida: "
                            + "ela possui intervencoes registradas no historico.");
        }

        equipeRepository.delete(equipe);
    }

    /**
     * Carrega a entidade gerenciada ou lança 404.
     *
     * Método interno ao pacote de serviço: os outros Services usam este
     * ponto único para resolver um id de equipe, em vez de cada um repetir
     * o próprio findById().orElseThrow().
     */
    EquipeManutencao obterOuFalhar(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Equipe", id));
    }

    private void validarNomeDisponivel(String nome, Long idAtual) {
        equipeRepository.findByNomeIgnoreCase(nome.trim()).ifPresent(existente -> {
            if (!existente.getId().equals(idAtual)) {
                throw new RegraDeNegocioException(
                        "Ja existe uma equipe chamada '" + existente.getNome() + "'.");
            }
        });
    }
}
