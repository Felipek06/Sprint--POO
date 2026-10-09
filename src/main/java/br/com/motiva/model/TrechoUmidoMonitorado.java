package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Trecho úmido equipado com sensor IoT de monitoramento vegetal.
 *
 * Demonstra a combinação de herança com interface:
 *  - É um TrechoUmido, logo herda o comportamento de crescimento acelerado;
 *  - Implementa MonitoravelViaIoT, logo adquire a capacidade de transmitir
 *    dados.
 *
 * Repare que esta é uma herança de dois níveis
 * (TrechoRodovia -> TrechoUmido -> TrechoUmidoMonitorado) e o JPA lida com
 * ela sem nenhuma configuração extra: continua tudo na mesma tabela,
 * separado pelo valor da coluna TIPO_TRECHO.
 */
@Entity
@DiscriminatorValue(TrechoUmidoMonitorado.TIPO)
public class TrechoUmidoMonitorado extends TrechoUmido implements MonitoravelViaIoT {

    public static final String TIPO = "UMIDO_MONITORADO";

    /** Margem de erro máxima do sensor (mais ou menos 5%). */
    private static final double MARGEM_ERRO_SENSOR = 0.05;

    @Column(name = "ID_SENSOR", length = 50)
    private String idSensor;

    protected TrechoUmidoMonitorado() {
    }

    public TrechoUmidoMonitorado(Integer quilometroInicial, Integer quilometroFinal,
                                 Double nivelVegetacaoCm, Double indicePluviometrico,
                                 String idSensor) {
        super(TIPO, quilometroInicial, quilometroFinal, nivelVegetacaoCm, indicePluviometrico);
        this.idSensor = idSensor;
    }

    /**
     * Simula uma leitura do sensor com pequena variação aleatória, como
     * acontece em sensores reais de campo.
     *
     * Diferença para a Sprint 3: lá este método imprimia a leitura com
     * System.out.printf. Aqui ele apenas devolve o número. Quem decide o que
     * fazer com ele é o RelatorioPrioridadeService, e quem o exibe é o JSON.
     */
    @Override
    public double transmitirDadosSensor() {
        double nivelReal = getNivelVegetacaoCm() == null ? 0.0 : getNivelVegetacaoCm();
        double variacao = 1.0 + ThreadLocalRandom.current().nextDouble(-MARGEM_ERRO_SENSOR, MARGEM_ERRO_SENSOR);
        return nivelReal * variacao;
    }

    @Override
    public String getIdSensor() {
        return idSensor;
    }

    public void setIdSensor(String idSensor) {
        this.idSensor = idSensor;
    }

    @Override
    public String getDescricaoTipo() {
        return "Trecho Úmido Monitorado (IoT)";
    }
}
