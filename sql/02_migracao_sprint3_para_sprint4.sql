-- =====================================================================
-- MOTIVA - Sprint 4
-- Migracao das tabelas da Sprint 3 (IDENTITY) para o modelo da Sprint 4
-- (SEQUENCES), preservando TODOS os dados ja gravados.
--
-- Rode este script UMA UNICA VEZ, conectado com o mesmo usuario que
-- criou as tabelas da Sprint 3.
--
-- Por que isso e necessario?
--   Na Sprint 3 o ID era GENERATED ALWAYS AS IDENTITY: o Oracle gerava o
--   valor e o Java nem tomava conhecimento. Na Sprint 4 o enunciado exige
--   @GeneratedValue(strategy = SEQUENCE) + @SequenceGenerator, ou seja, o
--   Hibernate precisa poder pedir o proximo valor a uma sequence NOMEADA
--   e escrever esse valor na coluna ID. Com GENERATED ALWAYS o Oracle
--   recusa qualquer INSERT que informe o ID (ORA-32795), entao a identity
--   precisa sair e dar lugar a uma sequence.
--
-- O bloco abaixo e idempotente: pode ser executado de novo sem erro.
-- =====================================================================

SET SERVEROUTPUT ON;

DECLARE
    -- Tabelas da Sprint 3 e as sequences equivalentes da Sprint 4
    TYPE t_nome IS VARRAY(4) OF VARCHAR2(40);

    v_tabelas   t_nome := t_nome('EQUIPE_MANUTENCAO',
                                 'TRECHO_RODOVIA',
                                 'INTERVENCAO_OPERACIONAL',
                                 'RELATORIO_PRIORIDADE');

    v_sequences t_nome := t_nome('SEQ_EQUIPE_MANUTENCAO',
                                 'SEQ_TRECHO_RODOVIA',
                                 'SEQ_INTERVENCAO_OPERACIONAL',
                                 'SEQ_RELATORIO_PRIORIDADE');

    v_eh_identity   NUMBER;
    v_existe_seq    NUMBER;
    v_proximo_id    NUMBER;
BEGIN
    FOR i IN 1 .. v_tabelas.COUNT LOOP

        ------------------------------------------------------------------
        -- 1) Remove a IDENTITY da coluna ID, se ainda existir
        ------------------------------------------------------------------
        SELECT COUNT(*)
          INTO v_eh_identity
          FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME     = v_tabelas(i)
           AND COLUMN_NAME    = 'ID'
           AND IDENTITY_COLUMN = 'YES';

        IF v_eh_identity > 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || v_tabelas(i) || ' MODIFY (ID DROP IDENTITY)';
            DBMS_OUTPUT.PUT_LINE('IDENTITY removida de ' || v_tabelas(i) || '.ID');
        ELSE
            DBMS_OUTPUT.PUT_LINE(v_tabelas(i) || '.ID ja nao era IDENTITY - nada a fazer');
        END IF;

        ------------------------------------------------------------------
        -- 2) Cria a sequence comecando DEPOIS do maior ID ja gravado,
        --    para nao colidir com as linhas da Sprint 3
        ------------------------------------------------------------------
        SELECT COUNT(*)
          INTO v_existe_seq
          FROM USER_SEQUENCES
         WHERE SEQUENCE_NAME = v_sequences(i);

        IF v_existe_seq = 0 THEN
            EXECUTE IMMEDIATE 'SELECT NVL(MAX(ID), 0) + 1 FROM ' || v_tabelas(i)
                INTO v_proximo_id;

            EXECUTE IMMEDIATE 'CREATE SEQUENCE ' || v_sequences(i) ||
                              ' START WITH ' || v_proximo_id ||
                              ' INCREMENT BY 1 NOCACHE NOCYCLE';

            DBMS_OUTPUT.PUT_LINE('Sequence ' || v_sequences(i) ||
                                 ' criada comecando em ' || v_proximo_id);
        ELSE
            DBMS_OUTPUT.PUT_LINE('Sequence ' || v_sequences(i) || ' ja existia - mantida');
        END IF;

    END LOOP;
END;
/

COMMIT;

-- ---------------------------------------------------------------------
-- Conferencia: as 4 sequences precisam aparecer aqui
-- ---------------------------------------------------------------------
SELECT SEQUENCE_NAME, LAST_NUMBER
  FROM USER_SEQUENCES
 WHERE SEQUENCE_NAME IN ('SEQ_EQUIPE_MANUTENCAO',
                         'SEQ_TRECHO_RODOVIA',
                         'SEQ_INTERVENCAO_OPERACIONAL',
                         'SEQ_RELATORIO_PRIORIDADE')
 ORDER BY SEQUENCE_NAME;
