package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro histórico de uma execução do motor de prioridade.
 *
 * Cada linha é o retrato (snapshot) de uma rodada do relatório: quantos
 * trechos estavam em cada faixa no instante em que ele foi gerado.
 *
 * Na Sprint 3 este conceito existia como um record aninhado dentro do
 * RelatorioPrioridadeDAO — ou seja, era um detalhe da camada de
 * persistência. Na Sprint 4 ele sobe para o pacote model e vira uma
 * entidade de primeira classe, porque agora o histórico é um recurso da
 * API, exposto em GET /api/relatorios.
 */
@Entity
@Table(name = "RELATORIO_PRIORIDADE")
public class RelatorioPrioridade {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqRelatorioPrioridade")
    @SequenceGenerator(
            name = "seqRelatorioPrioridade",
            sequenceName = "SEQ_RELATORIO_PRIORIDADE",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATA_GERACAO", nullable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "QT_URGENTE", nullable = false)
    private Integer qtUrgente;

    @Column(name = "QT_CRITICO", nullable = false)
    private Integer qtCritico;

    @Column(name = "QT_ATENCAO", nullable = false)
    private Integer qtAtencao;

    @Column(name = "QT_NORMAL", nullable = false)
    private Integer qtNormal;

    @Column(name = "RESUMO", length = 4000)
    private String resumo;

    protected RelatorioPrioridade() {
    }

    public RelatorioPrioridade(Integer qtUrgente, Integer qtCritico,
                               Integer qtAtencao, Integer qtNormal, String resumo) {
        this.qtUrgente = qtUrgente;
        this.qtCritico = qtCritico;
        this.qtAtencao = qtAtencao;
        this.qtNormal = qtNormal;
        this.resumo = resumo;
    }

    @PrePersist
    void aoPersistir() {
        if (dataGeracao == null) {
            dataGeracao = LocalDateTime.now();
        }
    }

    /** Total de trechos analisados nesta execução. */
    public int getTotalTrechosAnalisados() {
        return qtUrgente + qtCritico + qtAtencao + qtNormal;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public Integer getQtUrgente() {
        return qtUrgente;
    }

    public Integer getQtCritico() {
        return qtCritico;
    }

    public Integer getQtAtencao() {
        return qtAtencao;
    }

    public Integer getQtNormal() {
        return qtNormal;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }
}
