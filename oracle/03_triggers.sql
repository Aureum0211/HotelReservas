-- 03_triggers.sql
-- Ejecutar DESPUES de 02_funciones.sql (el trigger 1 usa fn_noches y el 2 usa fn_hay_solape).
-- Se puede ejecutar varias veces. Si recreas las tablas, repite 02_funciones.sql y despues este archivo.
-- Crea una tabla de auditoria, su secuencia y tres triggers sobre RESERVAS.
-- Los triggers refuerzan las reglas de la base de datos si alguien inserta o modifica reservas
-- por un camino distinto a los paquetes PL/SQL.

-- Comprobacion previa: sin las funciones los triggers se crean "con errores de compilacion"
-- (PLS-00201) y quedan inservibles. Si falta algo, este bloque se detiene con un mensaje claro.
DECLARE
    v_validas NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_validas FROM user_objects
     WHERE object_type = 'FUNCTION' AND status = 'VALID'
       AND object_name IN ('FN_NOCHES', 'FN_HAY_SOLAPE');
    IF v_validas < 2 THEN
        RAISE_APPLICATION_ERROR(-20020,
            'Faltan las funciones FN_NOCHES / FN_HAY_SOLAPE o estan invalidas. '
            || 'Ejecuta primero 02_funciones.sql (con las 5 tablas ya creadas).');
    END IF;
END;
/

-- Tabla y secuencia de auditoria (se crean solo si no existen: ORA-00955 se ignora).
-- No lleva llave foranea a proposito: un registro de auditoria debe sobrevivir aunque se borre la reserva.
BEGIN
    EXECUTE IMMEDIATE '
        CREATE TABLE auditoria_reservas (
            id              NUMBER(10)   CONSTRAINT pk_auditoria_reservas PRIMARY KEY,
            reserva_id      NUMBER(10)   NOT NULL,
            estado_anterior VARCHAR2(11),
            estado_nuevo    VARCHAR2(11) NOT NULL,
            usuario_bd      VARCHAR2(30) NOT NULL,
            fecha           DATE         DEFAULT SYSDATE NOT NULL
        )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE seq_auditoria_reservas START WITH 1 INCREMENT BY 1 NOCACHE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN RAISE; END IF;
END;
/

-- Trigger 1: valida cada reserva que se inserta o modifica.
--   -20001: la salida debe ser posterior a la entrada.
--   -20002: cambio de estado no permitido. Solo se aceptan los mismos que Java:
--           ACTIVE -> CHECKED_IN | CANCELLED   y   CHECKED_IN -> CHECKED_OUT.
CREATE OR REPLACE TRIGGER trg_reservas_validar
BEFORE INSERT OR UPDATE ON reservas
FOR EACH ROW
BEGIN
    IF fn_noches(:NEW.entrada, :NEW.salida) <= 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'La fecha de salida debe ser posterior a la de entrada.');
    END IF;

    IF UPDATING AND :OLD.estado <> :NEW.estado THEN
        IF NOT (   (:OLD.estado = 'ACTIVE'     AND :NEW.estado IN ('CHECKED_IN', 'CANCELLED'))
                OR (:OLD.estado = 'CHECKED_IN' AND :NEW.estado = 'CHECKED_OUT')) THEN
            RAISE_APPLICATION_ERROR(-20002,
                'Cambio de estado no permitido: ' || :OLD.estado || ' -> ' || :NEW.estado);
        END IF;
    END IF;
END trg_reservas_validar;
/

-- Trigger 2: impide que dos reservas vigentes se crucen en la misma habitacion (-20003).
-- Es un trigger COMPUESTO: un trigger normal por fila no puede consultar la tabla que esta
-- modificando (error ORA-04091, "tabla mutante"). Aqui se guardan las filas tocadas y se
-- comprueban al terminar la sentencia, cuando la tabla ya esta estable.
CREATE OR REPLACE TRIGGER trg_reservas_sin_solape
FOR INSERT OR UPDATE ON reservas
COMPOUND TRIGGER

    TYPE t_reserva IS RECORD (
        id            reservas.id%TYPE,
        habitacion_id reservas.habitacion_id%TYPE,
        entrada       reservas.entrada%TYPE,
        salida        reservas.salida%TYPE
    );
    TYPE t_lista IS TABLE OF t_reserva INDEX BY PLS_INTEGER;
    g_lista t_lista;

    AFTER EACH ROW IS
        v_fila t_reserva;
    BEGIN
        IF :NEW.estado IN ('ACTIVE', 'CHECKED_IN') THEN
            v_fila.id            := :NEW.id;
            v_fila.habitacion_id := :NEW.habitacion_id;
            v_fila.entrada       := :NEW.entrada;
            v_fila.salida        := :NEW.salida;
            g_lista(g_lista.COUNT + 1) := v_fila;
        END IF;
    END AFTER EACH ROW;

    AFTER STATEMENT IS
    BEGIN
        FOR i IN 1 .. g_lista.COUNT LOOP
            IF fn_hay_solape(g_lista(i).habitacion_id, g_lista(i).entrada,
                             g_lista(i).salida, g_lista(i).id) = 1 THEN
                RAISE_APPLICATION_ERROR(-20003,
                    'La habitacion ya tiene una reserva vigente en esas fechas.');
            END IF;
        END LOOP;
    END AFTER STATEMENT;

END trg_reservas_sin_solape;
/

-- Trigger 3: guarda en AUDITORIA_RESERVAS cada reserva nueva y cada cambio de estado.
CREATE OR REPLACE TRIGGER trg_reservas_auditoria
AFTER INSERT OR UPDATE OF estado ON reservas
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        INSERT INTO auditoria_reservas (id, reserva_id, estado_anterior, estado_nuevo, usuario_bd, fecha)
        VALUES (seq_auditoria_reservas.NEXTVAL, :NEW.id, NULL, :NEW.estado, USER, SYSDATE);
    ELSIF :OLD.estado <> :NEW.estado THEN
        INSERT INTO auditoria_reservas (id, reserva_id, estado_anterior, estado_nuevo, usuario_bd, fecha)
        VALUES (seq_auditoria_reservas.NEXTVAL, :NEW.id, :OLD.estado, :NEW.estado, USER, SYSDATE);
    END IF;
END trg_reservas_auditoria;
/

-- Comprobacion final.
-- 1) Los tres triggers deben decir VALID y ENABLED.
SELECT trigger_name, status FROM user_triggers
 WHERE trigger_name IN ('TRG_RESERVAS_VALIDAR', 'TRG_RESERVAS_SIN_SOLAPE', 'TRG_RESERVAS_AUDITORIA')
 ORDER BY trigger_name;

-- 2) Si esta consulta NO devuelve filas, todo compilo bien. Si devuelve filas, copiame ese texto.
SELECT name, line, text FROM user_errors
 WHERE name LIKE 'TRG_RESERVAS%'
 ORDER BY name, sequence;
