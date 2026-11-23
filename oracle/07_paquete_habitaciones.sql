-- 07_paquete_habitaciones.sql
-- Paquete PKG_HABITACIONES: registro de habitaciones y mantenimiento.
-- Reemplaza HotelService.registerRoom y HotelService.setRoomMaintenance (y Room.startMaintenance/finishMaintenance).
-- ORDEN: despues de 04_base_comun.sql. Se puede ejecutar varias veces. Ejecutalo como SCRIPT (F5).
--
-- Codigos de error:
--   -20070 ya existe una habitacion con ese numero     -20073 la habitacion no esta en mantenimiento
--   -20071 dato invalido (tipo, capacidad, precio)     -20074 tiene reservas activas
--   -20072 la habitacion ya esta en mantenimiento      -20030 la habitacion no existe
--   (-20050 / -20051: sp_exigir_admin; -20060: numero vacio o demasiado largo)
SET SERVEROUTPUT ON

CREATE OR REPLACE PACKAGE pkg_habitaciones AS

    -- Registra una habitacion disponible. Solo un ADMIN activo (p_actor_id). p_tipo: SINGLE, DOUBLE o SUITE.
    PROCEDURE sp_registrar_habitacion (
        p_actor_id IN  NUMBER,
        p_numero   IN  VARCHAR2,
        p_tipo     IN  VARCHAR2,
        p_capacidad IN NUMBER,
        p_precio   IN  NUMBER,
        p_id       OUT NUMBER
    );

    -- Pone la habitacion en mantenimiento. No se permite si tiene reservas activas (ACTIVE o CHECKED_IN).
    PROCEDURE sp_iniciar_mantenimiento (p_actor_id IN NUMBER, p_habitacion_id IN NUMBER);

    -- Saca la habitacion de mantenimiento.
    PROCEDURE sp_finalizar_mantenimiento (p_actor_id IN NUMBER, p_habitacion_id IN NUMBER);

END pkg_habitaciones;
/

CREATE OR REPLACE PACKAGE BODY pkg_habitaciones AS

    -- Bloquea la habitacion para cambiarla sin que otro usuario la modifique a la vez.
    PROCEDURE bloquear (p_id IN NUMBER, p_hab OUT habitaciones%ROWTYPE) IS
    BEGIN
        SELECT * INTO p_hab FROM habitaciones WHERE id = p_id FOR UPDATE;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20030, 'No existe la habitacion #' || p_id);
    END bloquear;

    PROCEDURE sp_registrar_habitacion (
        p_actor_id  IN  NUMBER,
        p_numero    IN  VARCHAR2,
        p_tipo      IN  VARCHAR2,
        p_capacidad IN  NUMBER,
        p_precio    IN  NUMBER,
        p_id        OUT NUMBER
    ) IS
        v_numero VARCHAR2(4000) := TRIM(p_numero);
        v_n      NUMBER;
    BEGIN
        sp_exigir_admin(p_actor_id);

        IF v_numero IS NULL THEN
            RAISE_APPLICATION_ERROR(-20060, 'El numero de habitacion es obligatorio.');
        END IF;
        IF LENGTH(v_numero) > 6 THEN
            RAISE_APPLICATION_ERROR(-20060, 'El numero de habitacion no puede superar 6 caracteres.');
        END IF;
        IF p_tipo IS NULL OR p_tipo NOT IN ('SINGLE', 'DOUBLE', 'SUITE') THEN
            RAISE_APPLICATION_ERROR(-20071, 'El tipo de habitacion no es valido.');
        END IF;
        IF p_capacidad IS NULL OR p_capacidad <> TRUNC(p_capacidad) OR p_capacidad < 1 THEN
            RAISE_APPLICATION_ERROR(-20071, 'La capacidad debe ser mayor que cero.');
        END IF;
        IF p_capacidad > 99 THEN
            RAISE_APPLICATION_ERROR(-20071, 'La capacidad no puede superar 99 personas.');
        END IF;
        IF p_precio IS NULL OR p_precio <= 0 THEN
            RAISE_APPLICATION_ERROR(-20071, 'El precio debe ser un numero mayor que cero.');
        END IF;
        IF ROUND(p_precio, 2) <= 0 THEN
            RAISE_APPLICATION_ERROR(-20071, 'El precio debe ser un numero mayor que cero.');
        END IF;
        IF p_precio >= 100000000 THEN
            RAISE_APPLICATION_ERROR(-20071, 'El precio es demasiado alto.');
        END IF;

        SELECT COUNT(*) INTO v_n FROM habitaciones WHERE UPPER(numero) = UPPER(v_numero);
        IF v_n > 0 THEN
            RAISE_APPLICATION_ERROR(-20070, 'Ya existe una habitacion con ese numero.');
        END IF;

        p_id := fn_siguiente_id('seq_habitaciones', 'habitaciones');
        INSERT INTO habitaciones (id, numero, tipo, capacidad, precio_noche, disponible, en_mantenimiento)
        VALUES (p_id, v_numero, p_tipo, p_capacidad, ROUND(p_precio, 2), 1, 0);
    END sp_registrar_habitacion;

    PROCEDURE sp_iniciar_mantenimiento (p_actor_id IN NUMBER, p_habitacion_id IN NUMBER) IS
        v_hab habitaciones%ROWTYPE;
        v_n   NUMBER;
    BEGIN
        sp_exigir_admin(p_actor_id);
        bloquear(p_habitacion_id, v_hab);
        IF v_hab.en_mantenimiento = 1 THEN
            RAISE_APPLICATION_ERROR(-20072, 'La habitacion ya esta en mantenimiento.');
        END IF;
        SELECT COUNT(*) INTO v_n FROM reservas
         WHERE habitacion_id = p_habitacion_id AND estado IN ('ACTIVE', 'CHECKED_IN');
        IF v_n > 0 THEN
            RAISE_APPLICATION_ERROR(-20074,
                'No se puede iniciar mantenimiento: la habitacion tiene reservas activas.');
        END IF;
        UPDATE habitaciones SET en_mantenimiento = 1 WHERE id = p_habitacion_id;
    END sp_iniciar_mantenimiento;

    PROCEDURE sp_finalizar_mantenimiento (p_actor_id IN NUMBER, p_habitacion_id IN NUMBER) IS
        v_hab habitaciones%ROWTYPE;
    BEGIN
        sp_exigir_admin(p_actor_id);
        bloquear(p_habitacion_id, v_hab);
        IF v_hab.en_mantenimiento = 0 THEN
            RAISE_APPLICATION_ERROR(-20073, 'La habitacion no esta en mantenimiento.');
        END IF;
        UPDATE habitaciones SET en_mantenimiento = 0 WHERE id = p_habitacion_id;
    END sp_finalizar_mantenimiento;

END pkg_habitaciones;
/

-- Comprobacion: ambos deben decir VALID, y la segunda consulta no debe devolver filas.
SELECT object_type, object_name, status FROM user_objects
 WHERE object_name = 'PKG_HABITACIONES' ORDER BY object_type;

SELECT name, type, line, text FROM user_errors WHERE name = 'PKG_HABITACIONES' ORDER BY type, sequence;
