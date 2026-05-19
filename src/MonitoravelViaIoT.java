/**
 * Contrato para trechos equipados com sensores IoT de monitoramento vegetal.
 *
 * Interface deliberadamente enxuta (Interface Segregation Principle):
 * define apenas o comportamento de transmissão — sem acoplamento a
 * hierarquia de classes, qualquer trecho pode adotar este contrato
 * independentemente de ser úmido, seco ou urbano.
 *
 * Diferença arquitetural chave:
 *  - Herança (extends)  → define O QUE o objeto É  (identidade/tipo)
 *  - Interface (implements) → define O QUE o objeto FAZ (capacidade/contrato)
 *
 * Um TrechoUmido já "é" um TrechoRodovia por herança.
 * Se ele também tiver sensor, ele "sabe transmitir dados" por esta interface —
 * sem precisar mudar sua hierarquia.
 */
public interface MonitoravelViaIoT {

    /**
     * Transmite os dados coletados pelo sensor instalado no trecho,
     * retornando a leitura de crescimento da vegetação em cm.
     *
     * @return leitura do sensor em cm (valor >= 0)
     */
    double transmitirDadosSensor();

    /**
     * Retorna o identificador único do sensor instalado no trecho.
     *
     * @return código do sensor (ex: "SENSOR-BR116-KM42")
     */
    String getIdSensor();
}