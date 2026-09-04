-- =====================================================================
-- MOTIVA - Sistema de Monitoramento e Priorização de Roçada
-- Script de criação das tabelas (Sprint 3)
-- Baseado nas classes: EquipeManutencao, TrechoRodovia (+ TrechoUmido/
-- TrechoSeco/TrechoUmidoMonitorado), IntervencaoOperacional
-- (+ RocadaMecanizada/Pulverizacao) e no relatório gerado por GeradorRelatorio
-- =====================================================================

-- Rode este script uma única vez (após conectar no Oracle do laboratório).
-- Se precisar recriar do zero, rode antes: @seu-script-drop.sql (opcional)

-- ---------------------------------------------------------------------
-- 1) EQUIPE_MANUTENCAO  <-  classe EquipeManutencao
-- ---------------------------------------------------------------------
CREATE TABLE EQUIPE_MANUTENCAO (
    ID                      NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NOME                    VARCHAR2(100)   NOT NULL,
    QUANTIDADE_INTEGRANTES  NUMBER(3)       NOT NULL,
    CONSTRAINT CK_EQUIPE_NOME_NAO_VAZIO CHECK (TRIM(NOME) IS NOT NULL),
    CONSTRAINT CK_EQUIPE_QTD_MINIMA     CHECK (QUANTIDADE_INTEGRANTES >= 1)
);

-- ---------------------------------------------------------------------
-- 2) TRECHO_RODOVIA  <-  TrechoRodovia / TrechoUmido / TrechoSeco /
--    TrechoUmidoMonitorado (hierarquia representada em uma única tabela,
--    diferenciada pela coluna TIPO_TRECHO)
-- ---------------------------------------------------------------------
CREATE TABLE TRECHO_RODOVIA (
    ID                      NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    QUILOMETRO_INICIAL      NUMBER(6)       NOT NULL,
    QUILOMETRO_FINAL        NUMBER(6)       NOT NULL,
    NIVEL_VEGETACAO_CM      NUMBER(7,2)     NOT NULL,
    TIPO_TRECHO             VARCHAR2(30)    NOT NULL,
    INDICE_PLUVIOMETRICO    NUMBER(4,2),        -- só para UMIDO / UMIDO_MONITORADO
    EM_ESTACAO_SECA         CHAR(1)         DEFAULT 'N',  -- só para SECO
    ID_SENSOR               VARCHAR2(50),       -- só para UMIDO_MONITORADO
    ID_EQUIPE_RESPONSAVEL   NUMBER,
    CONSTRAINT CK_TRECHO_KM        CHECK (QUILOMETRO_FINAL > QUILOMETRO_INICIAL),
    CONSTRAINT CK_TRECHO_KM_INI    CHECK (QUILOMETRO_INICIAL >= 0),
    CONSTRAINT CK_TRECHO_NIVEL     CHECK (NIVEL_VEGETACAO_CM >= 0),
    CONSTRAINT CK_TRECHO_TIPO      CHECK (TIPO_TRECHO IN ('UMIDO','SECO','UMIDO_MONITORADO')),
    CONSTRAINT CK_TRECHO_ESTACAO   CHECK (EM_ESTACAO_SECA IN ('S','N')),
    CONSTRAINT FK_TRECHO_EQUIPE    FOREIGN KEY (ID_EQUIPE_RESPONSAVEL)
                                    REFERENCES EQUIPE_MANUTENCAO(ID)
);

-- ---------------------------------------------------------------------
-- 3) INTERVENCAO_OPERACIONAL  <-  IntervencaoOperacional / RocadaMecanizada
--    / Pulverizacao
-- ---------------------------------------------------------------------
CREATE TABLE INTERVENCAO_OPERACIONAL (
    ID                      NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ID_TRECHO_ALVO          NUMBER          NOT NULL,
    ID_EQUIPE_RESPONSAVEL   NUMBER          NOT NULL,
    TIPO_INTERVENCAO        VARCHAR2(30)    NOT NULL,   -- ROCADA_MECANIZADA / PULVERIZACAO
    TIPO_PRODUTO            VARCHAR2(30),               -- só para PULVERIZACAO
    DATA_EXECUCAO           TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT CK_INTERV_TIPO   CHECK (TIPO_INTERVENCAO IN ('ROCADA_MECANIZADA','PULVERIZACAO')),
    CONSTRAINT FK_INTERV_TRECHO FOREIGN KEY (ID_TRECHO_ALVO)
                                 REFERENCES TRECHO_RODOVIA(ID),
    CONSTRAINT FK_INTERV_EQUIPE FOREIGN KEY (ID_EQUIPE_RESPONSAVEL)
                                 REFERENCES EQUIPE_MANUTENCAO(ID)
);

-- ---------------------------------------------------------------------
-- 4) RELATORIO_PRIORIDADE  <-  histórico gerado por GeradorRelatorio
-- ---------------------------------------------------------------------
CREATE TABLE RELATORIO_PRIORIDADE (
    ID                      NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    DATA_GERACAO            TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    QT_URGENTE              NUMBER(5)       NOT NULL,
    QT_CRITICO              NUMBER(5)       NOT NULL,
    QT_ATENCAO              NUMBER(5)       NOT NULL,
    QT_NORMAL               NUMBER(5)       NOT NULL,
    RESUMO                  VARCHAR2(4000)
);

COMMIT;