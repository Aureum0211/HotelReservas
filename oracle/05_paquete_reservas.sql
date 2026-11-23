-- 05_paquete_reservas.sql   (version 3: ahora Oracle es quien aplica TODAS las reglas de reservas y pagos)
-- Paquete PKG_RESERVAS: crear/cancelar reservas, llegada y salida, comprobantes, pagos, disponibilidad.
-- ORDEN: despues de 04_base_comun.sql (usa sp_exigir_admin y fn_siguiente_id). Se puede ejecutar varias veces.
-- Ejecutalo como SCRIPT (F5).
--
-- Reglas (las mismas que antes tenia Java en Reservation, Invoice y HotelService):
--   * total = precio x noches, -10% desde 7 noches, y -5% FREQUENT / -15% VIP (se acumulan).
--   * la entrada no puede ser anterior a hoy; la llegada solo desde la fecha de entrada y antes de la salida.
--   * el trabajador puede ser NULL (reserva hecha por el propio huesped).
--   * cancelar: solo si esta ACTIVE; los comprobantes PENDING / PARTIALLY_PAID quedan VOIDED y conservan
--     lo abonado (no hay reembolsos automaticos). Si ya hay pagos, solo un ADMIN puede cancelar.
--   * un huesped solo cancela SUS reservas y solo si todavia no tienen pagos.
--
-- Los procedimientos NO hacen COMMIT: quien los llama (Java con autocommit, o el usuario) confirma.
--
-- Codigos de error de este paquete (los de otros paquetes estan en los encabezados de los paquetes correspondientes):
--   -20030 la habitacion no existe        -20038 la reserva o el comprobante no existe
--   -20031 habitacion no disponible       -20039 la reserva ya tiene un comprobante vigente
--   -20032 supera la capacidad            -20040 la entrada es anterior a hoy
--   -20033 estado de reserva invalido     -20041 hoy esta fuera del rango de la reserva (llegada)
--   -20034 comprobante sin saldo          -20042 el cliente no existe
--   -20035 abono invalido                 -20052 la reserva no es de ese huesped
--   -20036 el cliente esta inactivo       -20053 la reserva ya tiene pagos (el huesped no puede cancelar)
--   -20037 trabajador inexistente/inactivo -20060 dato obligatorio o invalido
--   (-20001, -20003 y -20004 vienen de los triggers y funciones 02/03; -20050 y -20051 de sp_exigir_admin)
SET SERVEROUTPUT ON

-- Las secuencias ya las crea 04_base_comun.sql; este bloque tambien las garantiza si se ejecuta 05 por separado.
DECLARE
    PROCEDURE crear_seq(p_seq IN VARCHAR2, p_tabla IN VARCHAR2) IS
        v_inicio NUMBER;
    BEGIN
        EXECUTE IMMEDIATE 'SELECT NVL(MAX(id), 0) + 1 FROM ' || p_tabla INTO v_inicio;
        EXECUTE IMMEDIATE 'CREATE SEQUENCE ' || p_seq || ' START WITH ' || v_inicio
                          || ' INCREMENT BY 1 NOCACHE';
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -955 THEN RAISE; END IF;
    END crear_seq;
BEGIN
    crear_seq('seq_reservas', 'reservas');
    crear_seq('seq_comprobantes', 'comprobantes');
END;
/

-- Si venias de la version 2, el procedimiento sp_cancelar_reserva cambia de parametros: se vuelve a crear el paquete.
CREATE OR REPLACE PACKAGE pkg_reservas AS

    -- Porcentajes de descuento.
    c_noches_estadia_larga CONSTANT NUMBER := 7;
    c_desc_estadia_larga   CONSTANT NUMBER := 10;
    c_desc_frecuente       CONSTANT NUMBER := 5;
    c_desc_vip             CONSTANT NUMBER := 15;

    -- Total de una estadia aplicando los descuentos por duracion y por categoria del cliente.
    FUNCTION fn_calcular_total (
        p_precio_noche IN NUMBER,
        p_noches       IN NUMBER,
        p_categoria    IN VARCHAR2
    ) RETURN NUMBER;

    -- 1 si algun comprobante de la reserva tiene dinero abonado, 0 si no.
    FUNCTION fn_tiene_pagos (p_reserva_id IN NUMBER) RETURN NUMBER;

    -- Crea una reserva ACTIVE. p_trabajador_id puede ser NULL (autoservicio del huesped).
    PROCEDURE sp_crear_reserva (
        p_cliente_id    IN  NUMBER,
        p_trabajador_id IN  NUMBER,
        p_habitacion_id IN  NUMBER,
        p_entrada       IN  VARCHAR2,
        p_salida        IN  VARCHAR2,
        p_huespedes     IN  NUMBER,
        p_id            OUT NUMBER
    );

    -- Cancelacion hecha por un trabajador: ACTIVE -> CANCELLED. Si la reserva ya tiene pagos,
    -- p_trabajador_id debe ser un ADMIN activo (con pagos y sin admin: -20050 / -20051).
    PROCEDURE sp_cancelar_reserva (p_id IN NUMBER, p_trabajador_id IN NUMBER);

    -- Cancelacion hecha por el propio huesped: la reserva debe ser suya y no tener pagos.
    PROCEDURE sp_cancelar_reserva_huesped (p_cliente_id IN NUMBER, p_reserva_id IN NUMBER);

    -- ACTIVE -> CHECKED_IN (llegada), desde la fecha de entrada y antes de la salida.
    PROCEDURE sp_checkin (p_id IN NUMBER);

    -- CHECKED_IN -> CHECKED_OUT (salida).
    PROCEDURE sp_checkout (p_id IN NUMBER);

    -- Emite un comprobante PENDING por el total de la reserva.
    PROCEDURE sp_generar_comprobante (
        p_reserva_id IN  NUMBER,
        p_metodo     IN  VARCHAR2,
        p_id         OUT NUMBER
    );

    -- Suma un abono al comprobante: queda PARTIALLY_PAID o PAID.
    PROCEDURE sp_registrar_pago (p_comprobante_id IN NUMBER, p_monto IN NUMBER);

    -- Habitaciones libres (filas completas de HABITACIONES) para un rango y cantidad de huespedes,
    -- de la mas barata a la mas cara.
    FUNCTION fn_habitaciones_libres (
        p_entrada   IN VARCHAR2,
        p_salida    IN VARCHAR2,
        p_huespedes IN NUMBER DEFAULT 1
    ) RETURN SYS_REFCURSOR;

END pkg_reservas;
/

CREATE OR REPLACE PACKAGE BODY pkg_reservas AS

    -- Lee y bloquea la fila de la reserva (FOR UPDATE) para que dos usuarios no la cambien a la vez.
    FUNCTION bloquear_reserva (p_id IN NUMBER) RETURN reservas%ROWTYPE IS
        v_res reservas%ROWTYPE;
    BEGIN
        SELECT * INTO v_res FROM reservas WHERE id = p_id FOR UPDATE;
        RETURN v_res;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20038, 'No existe la reserva #' || p_id);
    END bloquear_reserva;

    -- Cancela la reserva y anula los comprobantes con saldo. Sin permisos: los comprueba quien llama.
    PROCEDURE cancelar_interno (p_id IN NUMBER) IS
        v_res reservas%ROWTYPE;
    BEGIN
        v_res := bloquear_reserva(p_id);
        IF v_res.estado = 'CANCELLED' THEN
            RAISE_APPLICATION_ERROR(-20033, 'La reserva ya esta cancelada.');
        ELSIF v_res.estado <> 'ACTIVE' THEN
            RAISE_APPLICATION_ERROR(-20033, 'No se puede cancelar una reserva que ya tiene la llegada registrada.');
        END IF;

        UPDATE reservas SET estado = 'CANCELLED' WHERE id = p_id;

        -- Los comprobantes con saldo se anulan y conservan lo abonado; los PAID se dejan como registro.
        UPDATE comprobantes SET estado = 'VOIDED'
         WHERE reserva_id = p_id AND estado IN ('PENDING', 'PARTIALLY_PAID');
    END cancelar_interno;

    FUNCTION fn_calcular_total (
        p_precio_noche IN NUMBER,
        p_noches       IN NUMBER,
        p_categoria    IN VARCHAR2
    ) RETURN NUMBER IS
        v_total NUMBER := p_precio_noche * p_noches;
    BEGIN
        IF p_noches >= c_noches_estadia_larga THEN
            v_total := v_total * (1 - c_desc_estadia_larga / 100);
        END IF;
        IF p_categoria = 'FREQUENT' THEN
            v_total := v_total * (1 - c_desc_frecuente / 100);
        ELSIF p_categoria = 'VIP' THEN
            v_total := v_total * (1 - c_desc_vip / 100);
        END IF;
        RETURN ROUND(v_total, 2);
    END fn_calcular_total;

    FUNCTION fn_tiene_pagos (p_reserva_id IN NUMBER) RETURN NUMBER IS
        v_n NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_n FROM comprobantes WHERE reserva_id = p_reserva_id AND pagado > 0;
        RETURN CASE WHEN v_n > 0 THEN 1 ELSE 0 END;
    END fn_tiene_pagos;

    PROCEDURE sp_crear_reserva (
        p_cliente_id    IN  NUMBER,
        p_trabajador_id IN  NUMBER,
        p_habitacion_id IN  NUMBER,
        p_entrada       IN  VARCHAR2,
        p_salida        IN  VARCHAR2,
        p_huespedes     IN  NUMBER,
        p_id            OUT NUMBER
    ) IS
        v_hab       habitaciones%ROWTYPE;
        v_activo    NUMBER;
        v_categoria clientes.categoria%TYPE;
        v_noches    NUMBER;
        v_entrada   VARCHAR2(10);
        v_salida    VARCHAR2(10);
    BEGIN
        IF p_entrada IS NULL OR p_salida IS NULL THEN
            RAISE_APPLICATION_ERROR(-20060, 'Las fechas de entrada y salida son obligatorias.');
        END IF;

        BEGIN
            SELECT activo, categoria INTO v_activo, v_categoria FROM clientes WHERE id = p_cliente_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20042, 'No existe el cliente #' || p_cliente_id);
        END;
        IF v_activo = 0 THEN
            RAISE_APPLICATION_ERROR(-20036, 'El cliente esta inactivo.');
        END IF;

        -- El trabajador es opcional: la reserva hecha por el propio huesped no lo tiene.
        IF p_trabajador_id IS NOT NULL THEN
            BEGIN
                SELECT activo INTO v_activo FROM trabajadores WHERE id = p_trabajador_id;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN v_activo := 0;
            END;
            IF v_activo = 0 THEN
                RAISE_APPLICATION_ERROR(-20037, 'El trabajador no existe o esta inactivo.');
            END IF;
        END IF;

        -- Bloquea la habitacion: dos reservas simultaneas de la misma habitacion se hacen en fila,
        -- y la segunda ya vera a la primera al comprobar el cruce.
        BEGIN
            SELECT * INTO v_hab FROM habitaciones WHERE id = p_habitacion_id FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20030, 'No existe la habitacion #' || p_habitacion_id);
        END;

        IF v_hab.en_mantenimiento = 1 THEN
            RAISE_APPLICATION_ERROR(-20031, 'La habitacion esta en mantenimiento y no se puede reservar.');
        END IF;
        IF v_hab.disponible = 0 THEN
            RAISE_APPLICATION_ERROR(-20031, 'La habitacion ' || v_hab.numero || ' no esta disponible.');
        END IF;
        IF p_huespedes IS NULL OR p_huespedes < 1 THEN
            RAISE_APPLICATION_ERROR(-20060, 'La cantidad de personas debe ser mayor que cero.');
        END IF;
        IF p_huespedes > v_hab.capacidad THEN
            RAISE_APPLICATION_ERROR(-20032,
                'La habitacion ' || v_hab.numero || ' admite maximo ' || v_hab.capacidad || ' personas.');
        END IF;

        v_noches := fn_noches(p_entrada, p_salida);   -- tambien valida el formato AAAA-MM-DD
        -- Normaliza las fechas para guardar siempre AAAA-MM-DD con ceros.
        v_entrada := TO_CHAR(TO_DATE(p_entrada, 'YYYY-MM-DD'), 'YYYY-MM-DD');
        v_salida  := TO_CHAR(TO_DATE(p_salida,  'YYYY-MM-DD'), 'YYYY-MM-DD');
        IF v_noches <= 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'La fecha de salida debe ser posterior a la de entrada.');
        END IF;
        IF TO_DATE(p_entrada, 'YYYY-MM-DD') < TRUNC(SYSDATE) THEN
            RAISE_APPLICATION_ERROR(-20040, 'La fecha de entrada no puede ser anterior a hoy.');
        END IF;
        IF fn_hay_solape(p_habitacion_id, p_entrada, p_salida) = 1 THEN
            RAISE_APPLICATION_ERROR(-20003, 'La habitacion ya esta reservada en esas fechas.');
        END IF;

        p_id := fn_siguiente_id('seq_reservas', 'reservas');
        INSERT INTO reservas (id, cliente_id, trabajador_id, habitacion_id, entrada, salida,
                              precio_noche, total, huespedes, estado)
        VALUES (p_id, p_cliente_id, p_trabajador_id, p_habitacion_id, v_entrada, v_salida,
                v_hab.precio_noche, fn_calcular_total(v_hab.precio_noche, v_noches, v_categoria),
                p_huespedes, 'ACTIVE');
    END sp_crear_reserva;

    PROCEDURE sp_cancelar_reserva (p_id IN NUMBER, p_trabajador_id IN NUMBER) IS
    BEGIN
        IF fn_tiene_pagos(p_id) = 1 THEN
            sp_exigir_admin(p_trabajador_id);
        END IF;
        cancelar_interno(p_id);
    END sp_cancelar_reserva;

    PROCEDURE sp_cancelar_reserva_huesped (p_cliente_id IN NUMBER, p_reserva_id IN NUMBER) IS
        v_res reservas%ROWTYPE;
    BEGIN
        v_res := bloquear_reserva(p_reserva_id);
        IF v_res.cliente_id <> p_cliente_id THEN
            RAISE_APPLICATION_ERROR(-20052, 'No encontramos esa reserva entre tus reservas.');
        END IF;
        IF fn_tiene_pagos(p_reserva_id) = 1 THEN
            RAISE_APPLICATION_ERROR(-20053, 'La reserva ya tiene pagos. Comunicate con recepcion para cancelarla.');
        END IF;
        cancelar_interno(p_reserva_id);
    END sp_cancelar_reserva_huesped;

    PROCEDURE sp_checkin (p_id IN NUMBER) IS
        v_res reservas%ROWTYPE;
        v_hoy DATE := TRUNC(SYSDATE);
    BEGIN
        v_res := bloquear_reserva(p_id);
        IF v_res.estado <> 'ACTIVE' THEN
            RAISE_APPLICATION_ERROR(-20033, 'Solo se puede registrar la llegada de una reserva confirmada.');
        END IF;
        IF v_hoy < TO_DATE(v_res.entrada, 'YYYY-MM-DD') THEN
            RAISE_APPLICATION_ERROR(-20041, 'Todavia no es la fecha de entrada (' || v_res.entrada || ').');
        END IF;
        IF v_hoy >= TO_DATE(v_res.salida, 'YYYY-MM-DD') THEN
            RAISE_APPLICATION_ERROR(-20041, 'La fecha de salida de esta reserva ya paso.');
        END IF;
        UPDATE reservas SET estado = 'CHECKED_IN' WHERE id = p_id;
    END sp_checkin;

    PROCEDURE sp_checkout (p_id IN NUMBER) IS
        v_res reservas%ROWTYPE;
    BEGIN
        v_res := bloquear_reserva(p_id);
        IF v_res.estado <> 'CHECKED_IN' THEN
            RAISE_APPLICATION_ERROR(-20033, 'Solo se puede registrar la salida de una reserva con llegada registrada.');
        END IF;
        UPDATE reservas SET estado = 'CHECKED_OUT' WHERE id = p_id;
    END sp_checkout;

    PROCEDURE sp_generar_comprobante (
        p_reserva_id IN  NUMBER,
        p_metodo     IN  VARCHAR2,
        p_id         OUT NUMBER
    ) IS
        v_res      reservas%ROWTYPE;
        v_vigentes NUMBER;
    BEGIN
        v_res := bloquear_reserva(p_reserva_id);
        IF v_res.estado = 'CANCELLED' THEN
            RAISE_APPLICATION_ERROR(-20033, 'No se puede facturar una reserva cancelada.');
        END IF;
        IF p_metodo IS NULL OR p_metodo NOT IN ('CASH', 'CARD', 'TRANSFER') THEN
            RAISE_APPLICATION_ERROR(-20060, 'El metodo de pago no es valido.');
        END IF;

        SELECT COUNT(*) INTO v_vigentes FROM comprobantes
         WHERE reserva_id = p_reserva_id AND estado <> 'VOIDED';
        IF v_vigentes > 0 THEN
            RAISE_APPLICATION_ERROR(-20039, 'La reserva #' || p_reserva_id || ' ya tiene un comprobante.');
        END IF;

        p_id := fn_siguiente_id('seq_comprobantes', 'comprobantes');
        INSERT INTO comprobantes (id, reserva_id, fecha_emision, monto, metodo_pago, estado, reembolso, pagado)
        VALUES (p_id, p_reserva_id, TO_CHAR(SYSDATE, 'YYYY-MM-DD'), v_res.total, p_metodo, 'PENDING', 0, 0);
    END sp_generar_comprobante;

    PROCEDURE sp_registrar_pago (p_comprobante_id IN NUMBER, p_monto IN NUMBER) IS
        v_comp comprobantes%ROWTYPE;
    BEGIN
        BEGIN
            SELECT * INTO v_comp FROM comprobantes WHERE id = p_comprobante_id FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20038, 'No existe el comprobante #' || p_comprobante_id);
        END;

        IF v_comp.estado NOT IN ('PENDING', 'PARTIALLY_PAID') THEN
            RAISE_APPLICATION_ERROR(-20034, 'El comprobante no tiene saldo pendiente.');
        END IF;
        IF p_monto IS NULL OR p_monto <= 0 THEN
            RAISE_APPLICATION_ERROR(-20035, 'El abono debe ser un numero mayor que cero.');
        END IF;
        IF p_monto <> ROUND(p_monto, 2) THEN
            RAISE_APPLICATION_ERROR(-20035, 'El abono admite maximo 2 decimales.');
        END IF;
        IF v_comp.pagado + p_monto > v_comp.monto THEN
            RAISE_APPLICATION_ERROR(-20035, 'El abono no puede superar el saldo pendiente.');
        END IF;

        UPDATE comprobantes
           SET pagado = pagado + p_monto,
               estado = CASE WHEN pagado + p_monto = monto THEN 'PAID' ELSE 'PARTIALLY_PAID' END
         WHERE id = p_comprobante_id;
    END sp_registrar_pago;

    FUNCTION fn_habitaciones_libres (
        p_entrada   IN VARCHAR2,
        p_salida    IN VARCHAR2,
        p_huespedes IN NUMBER DEFAULT 1
    ) RETURN SYS_REFCURSOR IS
        v_cur SYS_REFCURSOR;
    BEGIN
        IF p_entrada IS NULL OR p_salida IS NULL THEN
            RAISE_APPLICATION_ERROR(-20060, 'Las fechas de entrada y salida son obligatorias.');
        END IF;
        IF p_huespedes IS NULL OR p_huespedes < 1 THEN
            RAISE_APPLICATION_ERROR(-20060, 'La cantidad de personas debe ser mayor que cero.');
        END IF;
        IF fn_noches(p_entrada, p_salida) <= 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'La fecha de salida debe ser posterior a la de entrada.');
        END IF;
        OPEN v_cur FOR
            SELECT h.*
              FROM habitaciones h
             WHERE h.disponible = 1
               AND h.en_mantenimiento = 0
               AND h.capacidad >= p_huespedes
               AND fn_hay_solape(h.id, p_entrada, p_salida) = 0
             ORDER BY h.precio_noche, h.id;
        RETURN v_cur;
    END fn_habitaciones_libres;

END pkg_reservas;
/

-- Comprobacion: ambos deben decir VALID, y la segunda consulta no debe devolver filas.
SELECT object_type, object_name, status FROM user_objects
 WHERE object_name = 'PKG_RESERVAS' ORDER BY object_type;

SELECT name, type, line, text FROM user_errors WHERE name = 'PKG_RESERVAS' ORDER BY type, sequence;
