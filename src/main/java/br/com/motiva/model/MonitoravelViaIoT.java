package br.com.motiva.model;

/**
 * Contrato para trechos equipados com sensores IoT de monitoramento vegetal.
 *
 * Interface deliberadamente enxuta (Interface Segregation Principle):
 * define apenas o comportamento de transmissao, sem acoplamento a hierarquia
 * de classes. Qualquer trecho pode adotar este contrato, independentemente
 * de ser umido, seco ou urbano.
 *
 *  - Heranca (extends)      -> define O QUE o objeto E   (identidade/tipo)
 *  - Interface (implements) -> define O QUE o objeto FAZ (capacidade)
 *
 * Mudanca da Sprint 3 para a Sprint 4: transmitirDadosSensor() nao imprime
 * mais no console. Ela apenas DEVOLVE a leitura; quem decide o que fazer com
 * esse numero e a camada de servico, e quem o exibe e o JSON da resposta.
 */
public interface MonitoravelViaIoT {

    /**
     * Transmite os dados coletados pelo sensor instalado no trecho.
     *
     * @return leitura do sensor em cm (valor >= 0)
     */
    double transmitirDadosSensor();

    /**
     * @return codigo do sensor instalado (ex: "SENSOR-BR116-KM42")
     */
    String getIdSensor();
}
