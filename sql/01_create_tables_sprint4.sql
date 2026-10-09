-- =====================================================================
-- MOTIVA - Sprint 4 (Spring Boot + JPA + REST)
-- Script de criacao do zero: tabelas + SEQUENCES
--
-- Use este script apenas se voce quiser montar o schema do zero.
-- Se voce ja rodou o create_tables.sql da Sprint 3 (com IDENTITY),
-- rode no lugar deste o arquivo 02_migracao_sprint3_para_sprint4.sql.
--
-- Diferenca para a Sprint 3: a geracao de ID deixa de ser
-- GENERATED ALWAYS AS IDENTITY e passa a ser feita por SEQUENCE,
-- porque a Sprint 4 exige @GeneratedValue + @SequenceGenerator nas
-- entidades JPA (item 5.1 do enunciado).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) EQUIPE_MANUTENCAO  <-  model.EquipeManutencao
-- ---------------------------------------------------------------------
CREATE TABLE EQUIPE_MANUTENCAO (
    ID                      NUMBER          PRIMARY KEY,
    NOME                    VARCHAR2(100)   NOT NULL,
    QUANTIDADE_INTEGRANTES  NUMBER(3)       NOT NULL,
    CONSTRAINT CK_EQUIPE_NOME_NAO_VAZIO CHECK (TRIM(NOME) IS NOT NULL),
    CONSTRAINT CK_EQUIPE_QTD_MINIMA     CHECK (QUANTIDADE_INTEGRANTES >= 1)
);

CREATE SEQUENCE SEQ_EQUIPE_MANUTENCAO START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ---------------------------------------------------------------------
-- 2) TRECHO_RODOVIA  <-  model.TrechoRodovia (abstrata) e as subclasses
--    TrechoUmido / TrechoSeco / TrechoUmidoMonitorado.
--
--    Estrategia de heranca: SINGLE_TABLE (uma unica tabela para toda a
--    hierarquia), com a coluna TIPO_TRECHO como @DiscriminatorColumn.
-- ---------------------------------------------------------------------
CREATE TABLE TRECHO_RODOVIA (
    ID                      NUMBER          PRIMARY KEY,
    QUILOMETRO_INICIAL      NUMBER(6)       NOT NULL,
    QUILOMETRO_FINAL        NUMBER(6)       NOT NULL,
    NIVEL_VEGETACAO_CM      NUMBER(7,2)     NOT NULL,
    TIPO_TRECHO             VARCHAR2(30)    NOT NULL,
    INDICE_PLUVIOMETRICO    NUMBER(4,2),        -- so para UMIDO / UMIDO_MONITORADO
    EM_ESTACAO_SECA         CHAR(1)         DEFAULT 'N',  -- so para SECO
    ID_SENSOR               VARCHAR2(50),       -- so para UMIDO_MONITORADO
    ID_EQUIPE_RESPONSAVEL   NUMBER,
    CONSTRAINT CK_TRECHO_KM        CHECK (QUILOMETRO_FINAL > QUILOMETRO_INICIAL),
    CONSTRAINT CK_TRECHO_KM_INI    CHECK (QUILOMETRO_INICIAL >= 0),
    CONSTRAINT CK_TRECHO_NIVEL     CHECK (NIVEL_VEGETACAO_CM >= 0),
    CONSTRAINT CK_TRECHO_TIPO      CHECK (TIPO_TRECHO IN ('UMIDO','SECO','UMIDO_MONITORADO')),
    CONSTRAINT CK_TRECHO_ESTACAO   CHECK (EM_ESTACAO_SECA IN ('S','N')),
    CONSTRAINT FK_TRECHO_EQUIPE    FOREIGN KEY (ID_EQUIPE_RESPONSAVEL)
                                    REFERENCES EQUIPE_MANUTENCAO(ID)
);

CREATE SEQUENCE SEQ_TRECHO_RODOVIA START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ---------------------------------------------------------------------
-- 3) INTERVENCAO_OPERACIONAL  <-  model.IntervencaoOperacional (abstrata)
--    e as subclasses RocadaMecanizada / Pulverizacao.
--    Tambem SINGLE_TABLE, discriminador = TIPO_INTERVENCAO.
-- ---------------------------------------------------------------------
CREATE TABLE INTERVENCAO_OPERACIONAL (
    ID                      NUMBER          PRIMARY KEY,
    ID_TRECHO_ALVO          NUMBER          NOT NULL,
    ID_EQUIPE_RESPONSAVEL   NUMBER          NOT NULL,
    TIPO_INTERVENCAO        VARCHAR2(30)    NOT NULL,   -- ROCADA_MECANIZADA / PULVERIZACAO
    TIPO_PRODUTO            VARCHAR2(30),               -- so para PULVERIZACAO
    DATA_EXECUCAO           TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT CK_INTERV_TIPO   CHECK (TIPO_INTERVENCAO IN ('ROCADA_MECANIZADA','PULVERIZACAO')),
    CONSTRAINT FK_INTERV_TRECHO FOREIGN KEY (ID_TRECHO_ALVO)
                                 REFERENCES TRECHO_RODOVIA(ID),
    CONSTRAINT FK_INTERV_EQUIPE FOREIGN KEY (ID_EQUIPE_RESPONSAVEL)
                                 REFERENCES EQUIPE_MANUTENCAO(ID)
);

CREATE SEQUENCE SEQ_INTERVENCAO_OPERACIONAL START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ---------------------------------------------------------------------
-- 4) RELATORIO_PRIORIDADE  <-  historico gerado pelo motor de prioridade
-- ---------------------------------------------------------------------
CREATE TABLE RELATORIO_PRIORIDADE (
    ID                      NUMBER          PRIMARY KEY,
    DATA_GERACAO            TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    QT_URGENTE              NUMBER(5)       NOT NULL,
    QT_CRITICO              NUMBER(5)       NOT NULL,
    QT_ATENCAO              NUMBER(5)       NOT NULL,
    QT_NORMAL               NUMBER(5)       NOT NULL,
    RESUMO                  VARCHAR2(4000)
);

CREATE SEQUENCE SEQ_RELATORIO_PRIORIDADE START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

COMMIT;
