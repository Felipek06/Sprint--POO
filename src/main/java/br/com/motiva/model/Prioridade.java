package br.com.motiva.model;

/**
 * Faixas de prioridade de rocada, com o limiar e a recomendacao de cada uma.
 *
 * Na Sprint 3 este enum era privado, escondido dentro de GeradorRelatorio,
 * porque so o proprio relatorio o usava para montar texto de console. Na
 * Sprint 4 ele virou publico e ganhou o limiar como atributo, por dois
 * motivos:
 *
 *  1. o valor da classificacao agora sai no JSON, entao ele deixou de ser
 *     um detalhe interno e passou a fazer parte do contrato da API;
 *  2. os limiares deixam de ser constantes soltas e passam a viver junto
 *     da propria faixa que definem.
 *
 * Os limiares sao os mesmos da Sprint 2/3:
 *   >= 80 cm  URGENTE | >= 50 cm  CRITICO | >= 25 cm  ATENCAO | resto NORMAL
 */
public enum Prioridade {

    URGENTE(80.0, "Roçada Mecanizada — despachar equipe IMEDIATAMENTE"),
    CRITICO(50.0, "Pulverização herbicida + reavaliar em 7 dias"),
    ATENCAO(25.0, "Agendar roçada manual nas próximas 2 semanas"),
    NORMAL(0.0, "Monitoramento de rotina");

    private final double limiteInferiorCm;
    private final String recomendacao;

    Prioridade(double limiteInferiorCm, String recomendacao) {
        this.limiteInferiorCm = limiteInferiorCm;
        this.recomendacao = recomendacao;
    }

    /**
     * Classifica um nivel de vegetacao na faixa correspondente.
     *
     * As faixas sao avaliadas na ordem de declaracao (da mais grave para a
     * mais branda), entao a primeira que couber e a correta.
     *
     * @param nivelVegetacaoCm altura da vegetacao em cm
     * @return a faixa de prioridade correspondente
     */
    public static Prioridade classificar(double nivelVegetacaoCm) {
        for (Prioridade prioridade : values()) {
            if (nivelVegetacaoCm >= prioridade.limiteInferiorCm) {
                return prioridade;
            }
        }
        return NORMAL;
    }

    public double getLimiteInferiorCm() {
        return limiteInferiorCm;
    }

    public String getRecomendacao() {
        return recomendacao;
    }
}
