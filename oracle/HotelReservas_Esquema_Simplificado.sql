-- HOTELRESERVAS - ESQUEMA ORACLE
-- Ejecutar conectado al usuario/esquema HOTEL, en SQL Developer con F5.
-- Este archivo crea el esquema desde cero. No ejecutarlo sobre una base con datos existentes.

-- 1. TABLAS Y RESTRICCIONES
CREATE TABLE clientes (
    id NUMBER(10) CONSTRAINT pk_clientes PRIMARY KEY,
    nombre VARCHAR2(60 CHAR) NOT NULL,
    documento VARCHAR2(15 CHAR) NOT NULL CONSTRAINT uq_clientes_documento UNIQUE,
    correo VARCHAR2(50 CHAR) NOT NULL CONSTRAINT uq_clientes_correo UNIQUE,
    telefono VARCHAR2(10) NOT NULL,
    ciudad VARCHAR2(40 CHAR) NOT NULL,
    categoria VARCHAR2(8) NOT NULL CONSTRAINT ck_clientes_categoria CHECK (categoria IN ('NEW', 'FREQUENT', 'VIP')),
    activo NUMBER(1) DEFAULT 1 NOT NULL CONSTRAINT ck_clientes_activo CHECK (activo IN (0, 1))
);

CREATE TABLE trabajadores (
    id NUMBER(10) CONSTRAINT pk_trabajadores PRIMARY KEY,
    nombre VARCHAR2(60 CHAR) NOT NULL,
    documento VARCHAR2(15 CHAR) NOT NULL CONSTRAINT uq_trabajadores_documento UNIQUE,
    correo VARCHAR2(50 CHAR) NOT NULL CONSTRAINT uq_trabajadores_correo UNIQUE,
    telefono VARCHAR2(10) NOT NULL,
    ciudad VARCHAR2(40 CHAR) NOT NULL,
    activo NUMBER(1) DEFAULT 1 NOT NULL CONSTRAINT ck_trabajadores_activo CHECK (activo IN (0, 1)),
    rol VARCHAR2(12) NOT NULL CONSTRAINT ck_trabajadores_rol CHECK (rol IN ('RECEPTIONIST', 'ADMIN')),
    usuario VARCHAR2(20 CHAR) NOT NULL CONSTRAINT uq_trabajadores_usuario UNIQUE,
    contrasena VARCHAR2(30 CHAR) NOT NULL
);

CREATE TABLE habitaciones (
    id NUMBER(10) CONSTRAINT pk_habitaciones PRIMARY KEY,
    numero VARCHAR2(6 CHAR) NOT NULL CONSTRAINT uq_habitaciones_numero UNIQUE,
    tipo VARCHAR2(6) NOT NULL CONSTRAINT ck_habitaciones_tipo CHECK (tipo IN ('SINGLE', 'DOUBLE', 'SUITE')),
    capacidad NUMBER(2) NOT NULL CONSTRAINT ck_habitaciones_capacidad CHECK (capacidad > 0),
    precio_noche NUMBER(10,2) NOT NULL CONSTRAINT ck_habitaciones_precio CHECK (precio_noche > 0),
    disponible NUMBER(1) DEFAULT 1 NOT NULL CONSTRAINT ck_habitaciones_disponible CHECK (disponible IN (0, 1)),
    en_mantenimiento NUMBER(1) DEFAULT 0 NOT NULL CONSTRAINT ck_habitaciones_mant CHECK (en_mantenimiento IN (0, 1))
);

CREATE TABLE reservas (
    id NUMBER(10) CONSTRAINT pk_reservas PRIMARY KEY,
    cliente_id NUMBER(10) NOT NULL CONSTRAINT fk_reservas_cliente REFERENCES clientes(id),
    trabajador_id NUMBER(10) CONSTRAINT fk_reservas_trabajador REFERENCES trabajadores(id),
    habitacion_id NUMBER(10) NOT NULL CONSTRAINT fk_reservas_habitacion REFERENCES habitaciones(id),
    entrada VARCHAR2(10) NOT NULL,
    salida VARCHAR2(10) NOT NULL,
    precio_noche NUMBER(10,2) NOT NULL CONSTRAINT ck_reservas_precio CHECK (precio_noche > 0),
    total NUMBER(12,2) NOT NULL CONSTRAINT ck_reservas_total CHECK (total > 0),
    huespedes NUMBER(2) NOT NULL CONSTRAINT ck_reservas_huespedes CHECK (huespedes > 0),
    estado VARCHAR2(11) NOT NULL CONSTRAINT ck_reservas_estado CHECK (estado IN ('ACTIVE', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'))
);

CREATE TABLE comprobantes (
    id NUMBER(10) CONSTRAINT pk_comprobantes PRIMARY KEY,
    reserva_id NUMBER(10) NOT NULL CONSTRAINT fk_comprobantes_reserva REFERENCES reservas(id),
    fecha_emision VARCHAR2(10) NOT NULL,
    monto NUMBER(12,2) NOT NULL CONSTRAINT ck_comprobantes_monto CHECK (monto > 0),
    metodo_pago VARCHAR2(8) NOT NULL CONSTRAINT ck_comprobantes_metodo CHECK (metodo_pago IN ('CASH', 'CARD', 'TRANSFER')),
    estado VARCHAR2(14) NOT NULL CONSTRAINT ck_comprobantes_estado CHECK (estado IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'REFUNDED', 'VOIDED')) ,
    reembolso NUMBER(12,2) DEFAULT 0 NOT NULL CONSTRAINT ck_comprobantes_reembolso CHECK (reembolso >= 0 AND reembolso <= monto),
    pagado NUMBER(12,2) DEFAULT 0 NOT NULL CONSTRAINT ck_comprobantes_pagado CHECK (pagado >= 0 AND pagado <= monto)
);

CREATE TABLE auditoria_reservas (
    id NUMBER(10) CONSTRAINT pk_auditoria_reservas PRIMARY KEY,
    reserva_id NUMBER(10) NOT NULL,
    estado_anterior VARCHAR2(11),
    estado_nuevo VARCHAR2(11) NOT NULL,
    usuario_bd VARCHAR2(30) NOT NULL,
    fecha DATE DEFAULT SYSDATE NOT NULL
);

-- 2. SECUENCIAS Y DATOS INICIALES
CREATE SEQUENCE seq_clientes START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_trabajadores START WITH 5 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_habitaciones START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_reservas START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_comprobantes START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_auditoria_reservas START WITH 1 INCREMENT BY 1 NOCACHE;

INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
VALUES (1, 'Administrador', '1000000001', 'admin@hotel.com', '3001234567', 'Valledupar', 1, 'ADMIN', 'admin', 'admin123');
INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
VALUES (2, 'Laura Gomez', '1000000002', 'laura@hotel.com', '3002345678', 'Valledupar', 1, 'RECEPTIONIST', 'recepcion1', 'recep123');
INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
VALUES (3, 'Carlos Perez', '1000000003', 'carlos@hotel.com', '3003456789', 'Valledupar', 1, 'RECEPTIONIST', 'recepcion2', 'recep456');
INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
VALUES (4, 'Ana Torres', '1000000004', 'ana@hotel.com', '3004567890', 'Valledupar', 0, 'RECEPTIONIST', 'inactivo', 'inactivo123');
COMMIT;

-- 3. FUNCIONES DE APOYO
CREATE OR REPLACE FUNCTION fn_noches (
    p_entrada IN VARCHAR2,
    p_salida  IN VARCHAR2
) RETURN NUMBER IS
BEGIN
    RETURN TO_DATE(p_salida, 'YYYY-MM-DD') - TO_DATE(p_entrada, 'YYYY-MM-DD');
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20004, 'Fecha invalida. Use el formato AAAA-MM-DD.');
END fn_noches;
/

-- Devuelve 1 si la habitacion ya tiene OTRA reserva vigente (ACTIVE o CHECKED_IN) que se cruza
-- con las fechas dadas, y 0 si esta libre. p_excluir_id es la reserva que se esta guardando
-- (para no compararla consigo misma). Entrar el dia que otro sale NO es un cruce.
CREATE OR REPLACE FUNCTION fn_hay_solape (
    p_habitacion_id IN NUMBER,
    p_entrada       IN VARCHAR2,
    p_salida        IN VARCHAR2,
    p_excluir_id    IN NUMBER DEFAULT NULL
) RETURN NUMBER IS
    v_cantidad NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_cantidad
      FROM reservas
     WHERE habitacion_id = p_habitacion_id
       AND estado IN ('ACTIVE', 'CHECKED_IN')
       AND (p_excluir_id IS NULL OR id <> p_excluir_id)
       AND TO_DATE(entrada, 'YYYY-MM-DD') < TO_DATE(p_salida,  'YYYY-MM-DD')
       AND TO_DATE(salida,  'YYYY-MM-DD') > TO_DATE(p_entrada, 'YYYY-MM-DD');
    IF v_cantidad > 0 THEN
        RETURN 1;
    END IF;
    RETURN 0;
END fn_hay_solape;
/

-- Comprobacion: las dos funciones deben decir VALID. Si esta consulta devuelve filas en
-- la segunda parte, el problema esta en el texto de la columna TEXT.
SELECT object_name, status FROM user_objects
 WHERE object_type = 'FUNCTION' AND object_name IN ('FN_NOCHES', 'FN_HAY_SOLAPE')
 ORDER BY object_name;

SELECT name, line, text FROM user_errors
 WHERE name IN ('FN_NOCHES', 'FN_HAY_SOLAPE')
 ORDER BY name, sequence;

-- 4. FUNCIONES Y PROCEDIMIENTO COMPARTIDOS
-- Siguiente id de una secuencia, saltando los ids que ya existan en la tabla.
CREATE OR REPLACE FUNCTION fn_siguiente_id (
    p_secuencia IN VARCHAR2,
    p_tabla     IN VARCHAR2
) RETURN NUMBER IS
    v_seq VARCHAR2(128) := DBMS_ASSERT.SIMPLE_SQL_NAME(p_secuencia);
    v_tab VARCHAR2(128) := DBMS_ASSERT.SIMPLE_SQL_NAME(p_tabla);
    v_id  NUMBER;
    v_n   NUMBER;
BEGIN
    LOOP
        EXECUTE IMMEDIATE 'SELECT ' || v_seq || '.NEXTVAL FROM dual' INTO v_id;
        EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || v_tab || ' WHERE id = :1' INTO v_n USING v_id;
        EXIT WHEN v_n = 0;
    END LOOP;
    RETURN v_id;
END fn_siguiente_id;
/

-- Rol de un trabajador activo; NULL si no existe o esta inactivo.
CREATE OR REPLACE FUNCTION fn_rol_trabajador (p_id IN NUMBER) RETURN VARCHAR2 IS
    v_rol    trabajadores.rol%TYPE;
    v_activo trabajadores.activo%TYPE;
BEGIN
    SELECT rol, activo INTO v_rol, v_activo FROM trabajadores WHERE id = p_id;
    IF v_activo = 0 THEN
        RETURN NULL;
    END IF;
    RETURN v_rol;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
END fn_rol_trabajador;
/

-- Permisos: exige que quien actua sea un ADMIN activo.
--   -20051 el trabajador no existe o esta inactivo     -20050 no tiene el grado requerido
CREATE OR REPLACE PROCEDURE sp_exigir_admin (p_trabajador_id IN NUMBER) IS
    v_rol trabajadores.rol%TYPE;
BEGIN
    v_rol := fn_rol_trabajador(p_trabajador_id);
    IF v_rol IS NULL THEN
        RAISE_APPLICATION_ERROR(-20051, 'El trabajador no existe o esta inactivo.');
    END IF;
    IF v_rol <> 'ADMIN' THEN
        RAISE_APPLICATION_ERROR(-20050, 'Esta operacion requiere el grado Administrador o superior.');
    END IF;
END sp_exigir_admin;
/

-- 5. TRIGGERS DE VALIDACION Y AUDITORIA
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

-- 6. PAQUETE DE RESERVAS
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

-- 7. PAQUETE DE PERSONAS
CREATE OR REPLACE PACKAGE pkg_personas AS

    -- Registra un cliente (lo usa recepcion; la categoria la elige el trabajador).
    PROCEDURE sp_registrar_cliente (
        p_nombre    IN  VARCHAR2,
        p_documento IN  VARCHAR2,
        p_correo    IN  VARCHAR2,
        p_telefono  IN  VARCHAR2,
        p_ciudad    IN  VARCHAR2,
        p_categoria IN  VARCHAR2,
        p_id        OUT NUMBER
    );

    -- Registra a un huesped que se inscribe solo: siempre entra como NEW (nadie se asigna descuentos).
    PROCEDURE sp_registrar_huesped (
        p_nombre    IN  VARCHAR2,
        p_documento IN  VARCHAR2,
        p_correo    IN  VARCHAR2,
        p_telefono  IN  VARCHAR2,
        p_ciudad    IN  VARCHAR2,
        p_id        OUT NUMBER
    );

    -- Registra un trabajador. p_actor_id debe ser un ADMIN activo. p_rol: RECEPTIONIST o ADMIN.
    PROCEDURE sp_registrar_trabajador (
        p_actor_id   IN  NUMBER,
        p_nombre     IN  VARCHAR2,
        p_documento  IN  VARCHAR2,
        p_correo     IN  VARCHAR2,
        p_telefono   IN  VARCHAR2,
        p_ciudad     IN  VARCHAR2,
        p_rol        IN  VARCHAR2,
        p_usuario    IN  VARCHAR2,
        p_contrasena IN  VARCHAR2,
        p_id         OUT NUMBER
    );

    -- Devuelve el id del trabajador ACTIVO cuyo usuario (sin distinguir mayusculas) y contrasena coinciden.
    FUNCTION fn_login (p_usuario IN VARCHAR2, p_contrasena IN VARCHAR2) RETURN NUMBER;

    -- Devuelve el id del cliente con ese documento.
    FUNCTION fn_buscar_cliente_por_documento (p_documento IN VARCHAR2) RETURN NUMBER;

END pkg_personas;
/

CREATE OR REPLACE PACKAGE BODY pkg_personas AS

    -- Texto obligatorio, sin espacios sobrantes y con un maximo de caracteres.
    FUNCTION texto (p_valor IN VARCHAR2, p_campo IN VARCHAR2, p_max IN NUMBER) RETURN VARCHAR2 IS
        v_texto VARCHAR2(4000) := TRIM(p_valor);
    BEGIN
        IF v_texto IS NULL THEN
            RAISE_APPLICATION_ERROR(-20060, p_campo || ' es obligatorio.');
        END IF;
        IF LENGTH(v_texto) > p_max THEN
            RAISE_APPLICATION_ERROR(-20060, p_campo || ' no puede superar ' || p_max || ' caracteres.');
        END IF;
        RETURN v_texto;
    END texto;

    -- Correo con formato valido; se guarda en minusculas.
    FUNCTION correo (p_valor IN VARCHAR2) RETURN VARCHAR2 IS
        v_correo VARCHAR2(4000) := texto(p_valor, 'El correo', 50);
        v_arroba NUMBER := INSTR(v_correo, '@');
        v_ultima NUMBER := INSTR(v_correo, '@', -1);
        v_punto  NUMBER := INSTR(v_correo, '.', -1);
    BEGIN
        IF v_arroba <= 1 OR v_arroba <> v_ultima OR v_punto < v_arroba + 2
           OR v_punto = LENGTH(v_correo) OR INSTR(v_correo, ' ') > 0 THEN
            RAISE_APPLICATION_ERROR(-20064, 'El correo no tiene un formato valido.');
        END IF;
        RETURN LOWER(v_correo);
    END correo;

    -- Telefono de exactamente 10 digitos.
    FUNCTION telefono (p_valor IN VARCHAR2) RETURN VARCHAR2 IS
        v_tel VARCHAR2(4000) := texto(p_valor, 'El telefono', 10);
    BEGIN
        IF LENGTH(v_tel) <> 10 THEN
            RAISE_APPLICATION_ERROR(-20065, 'El telefono debe tener exactamente 10 digitos.');
        END IF;
        IF NOT REGEXP_LIKE(v_tel, '^[0-9]{10}$') THEN
            RAISE_APPLICATION_ERROR(-20065, 'El telefono solo puede contener digitos.');
        END IF;
        RETURN v_tel;
    END telefono;

    -- Documento, correo y telefono no pueden repetirse entre TODAS las personas (clientes y trabajadores).
    PROCEDURE asegurar_unica (p_documento IN VARCHAR2, p_correo IN VARCHAR2, p_telefono IN VARCHAR2) IS
        v_n NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_n FROM (SELECT documento AS dato FROM clientes
                                       UNION ALL SELECT documento FROM trabajadores)
         WHERE UPPER(dato) = UPPER(p_documento);
        IF v_n > 0 THEN
            RAISE_APPLICATION_ERROR(-20061, 'Ya existe una persona con ese documento.');
        END IF;

        SELECT COUNT(*) INTO v_n FROM (SELECT correo AS dato FROM clientes
                                       UNION ALL SELECT correo FROM trabajadores)
         WHERE UPPER(dato) = UPPER(p_correo);
        IF v_n > 0 THEN
            RAISE_APPLICATION_ERROR(-20062, 'Ya existe una persona con ese correo.');
        END IF;

        SELECT COUNT(*) INTO v_n FROM (SELECT telefono AS dato FROM clientes
                                       UNION ALL SELECT telefono FROM trabajadores)
         WHERE dato = p_telefono;
        IF v_n > 0 THEN
            RAISE_APPLICATION_ERROR(-20063, 'Ya existe una persona con ese telefono.');
        END IF;
    END asegurar_unica;

    PROCEDURE sp_registrar_cliente (
        p_nombre    IN  VARCHAR2,
        p_documento IN  VARCHAR2,
        p_correo    IN  VARCHAR2,
        p_telefono  IN  VARCHAR2,
        p_ciudad    IN  VARCHAR2,
        p_categoria IN  VARCHAR2,
        p_id        OUT NUMBER
    ) IS
        v_nombre    VARCHAR2(4000);
        v_documento VARCHAR2(4000);
        v_correo    VARCHAR2(4000);
        v_telefono  VARCHAR2(4000);
        v_ciudad    VARCHAR2(4000);
    BEGIN
        v_nombre    := texto(p_nombre, 'El nombre', 60);
        v_documento := texto(p_documento, 'El documento', 15);
        v_correo    := correo(p_correo);
        v_telefono  := telefono(p_telefono);
        v_ciudad    := texto(p_ciudad, 'La ciudad', 40);
        IF p_categoria IS NULL OR p_categoria NOT IN ('NEW', 'FREQUENT', 'VIP') THEN
            RAISE_APPLICATION_ERROR(-20067, 'La categoria no es valida.');
        END IF;
        asegurar_unica(v_documento, v_correo, v_telefono);

        p_id := fn_siguiente_id('seq_clientes', 'clientes');
        INSERT INTO clientes (id, nombre, documento, correo, telefono, ciudad, categoria, activo)
        VALUES (p_id, v_nombre, v_documento, v_correo, v_telefono, v_ciudad, p_categoria, 1);
    END sp_registrar_cliente;

    PROCEDURE sp_registrar_huesped (
        p_nombre    IN  VARCHAR2,
        p_documento IN  VARCHAR2,
        p_correo    IN  VARCHAR2,
        p_telefono  IN  VARCHAR2,
        p_ciudad    IN  VARCHAR2,
        p_id        OUT NUMBER
    ) IS
    BEGIN
        sp_registrar_cliente(p_nombre, p_documento, p_correo, p_telefono, p_ciudad, 'NEW', p_id);
    END sp_registrar_huesped;

    PROCEDURE sp_registrar_trabajador (
        p_actor_id   IN  NUMBER,
        p_nombre     IN  VARCHAR2,
        p_documento  IN  VARCHAR2,
        p_correo     IN  VARCHAR2,
        p_telefono   IN  VARCHAR2,
        p_ciudad     IN  VARCHAR2,
        p_rol        IN  VARCHAR2,
        p_usuario    IN  VARCHAR2,
        p_contrasena IN  VARCHAR2,
        p_id         OUT NUMBER
    ) IS
        v_nombre     VARCHAR2(4000);
        v_documento  VARCHAR2(4000);
        v_correo     VARCHAR2(4000);
        v_telefono   VARCHAR2(4000);
        v_ciudad     VARCHAR2(4000);
        v_usuario    VARCHAR2(4000);
        v_contrasena VARCHAR2(4000);
        v_n          NUMBER;
    BEGIN
        sp_exigir_admin(p_actor_id);

        v_nombre     := texto(p_nombre, 'El nombre', 60);
        v_documento  := texto(p_documento, 'El documento', 15);
        v_correo     := correo(p_correo);
        v_telefono   := telefono(p_telefono);
        v_ciudad     := texto(p_ciudad, 'La ciudad', 40);
        v_usuario    := texto(p_usuario, 'El usuario', 20);
        v_contrasena := texto(p_contrasena, 'La contrasena', 30);
        IF p_rol IS NULL OR p_rol NOT IN ('RECEPTIONIST', 'ADMIN') THEN
            RAISE_APPLICATION_ERROR(-20067, 'El rol no es valido.');
        END IF;
        asegurar_unica(v_documento, v_correo, v_telefono);

        SELECT COUNT(*) INTO v_n FROM trabajadores WHERE UPPER(usuario) = UPPER(v_usuario);
        IF v_n > 0 THEN
            RAISE_APPLICATION_ERROR(-20066, 'Ya existe un trabajador con ese usuario.');
        END IF;

        p_id := fn_siguiente_id('seq_trabajadores', 'trabajadores');
        INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
        VALUES (p_id, v_nombre, v_documento, v_correo, v_telefono, v_ciudad, 1, p_rol, v_usuario, v_contrasena);
    END sp_registrar_trabajador;

    FUNCTION fn_login (p_usuario IN VARCHAR2, p_contrasena IN VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        SELECT id INTO v_id
          FROM trabajadores
         WHERE UPPER(usuario) = UPPER(TRIM(p_usuario))
           AND contrasena = p_contrasena
           AND activo = 1
           AND ROWNUM = 1;
        RETURN v_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20068, 'Usuario o contrasena incorrectos.');
    END fn_login;

    FUNCTION fn_buscar_cliente_por_documento (p_documento IN VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
    BEGIN
        SELECT id INTO v_id
          FROM clientes
         WHERE UPPER(documento) = UPPER(TRIM(p_documento))
           AND ROWNUM = 1;
        RETURN v_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20069, 'No hay un huesped registrado con ese documento. Registrate primero.');
    END fn_buscar_cliente_por_documento;

END pkg_personas;
/

-- 8. PAQUETE DE HABITACIONES
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

-- 9. PAQUETE DE REPORTES
CREATE OR REPLACE PACKAGE pkg_reportes AS

    -- Una fila con: total_habitaciones, ocupadas, reservadas, libres, mantenimiento, no_disponibles
    -- (situacion de HOY), ingresos (abonado - reembolsado, de todos los comprobantes) y saldo_pendiente.
    FUNCTION fn_estadisticas_generales (p_actor_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Filas: estado, cantidad, valor_total. Siempre las 4 (ACTIVE, CHECKED_IN, CHECKED_OUT, CANCELLED).
    FUNCTION fn_reservas_por_estado (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR;

    -- Filas: mes (AAAA-MM), comprobantes, cobrado, reembolsado, neto. Mes mas reciente primero.
    FUNCTION fn_ingresos_por_mes (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR;

    -- Filas: numero, tipo, veces, noches, valor. TODAS las habitaciones (con 0 si no se usaron), sin contar
    -- reservas canceladas, de la mas a la menos usada.
    FUNCTION fn_uso_habitaciones (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR;

    -- Filas: nombre, categoria, cant_reservas, noches, valor. Los 10 clientes con mayor valor reservado
    -- (sin contar reservas canceladas).
    FUNCTION fn_mejores_clientes (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR;

    -- Filas: comprobante_id, cliente, fecha_emision, monto, pagado, saldo, estado (solo PENDING y PARTIALLY_PAID).
    FUNCTION fn_saldos_pendientes (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR;

    -- Ranking de habitaciones mas usadas (cuenta cada reserva NO cancelada con entrada desde p_desde).
    -- Filas: id, numero, tipo, veces, noches. Orden: mas reservas, luego mas noches, luego numero.
    -- p_limite NULL = todas las habitaciones. Sin control de rol.
    FUNCTION fn_top_habitaciones (
        p_desde  IN VARCHAR2 DEFAULT NULL,
        p_limite IN NUMBER   DEFAULT NULL
    ) RETURN SYS_REFCURSOR;

END pkg_reportes;
/

CREATE OR REPLACE PACKAGE BODY pkg_reportes AS

    -- Devuelve la fecha como texto AAAA-MM-DD normalizado (o NULL si no se dio).
    FUNCTION fecha_opcional (p_valor IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        IF p_valor IS NULL OR TRIM(p_valor) IS NULL THEN
            RETURN NULL;
        END IF;
        RETURN TO_CHAR(TO_DATE(TRIM(p_valor), 'YYYY-MM-DD'), 'YYYY-MM-DD');
    EXCEPTION
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20004, 'Fecha invalida. Use el formato AAAA-MM-DD.');
    END fecha_opcional;

    -- Valida el rango opcional y lo deja normalizado.
    PROCEDURE normalizar_rango (
        p_desde_in IN  VARCHAR2,
        p_hasta_in IN  VARCHAR2,
        p_desde    OUT VARCHAR2,
        p_hasta    OUT VARCHAR2
    ) IS
    BEGIN
        p_desde := fecha_opcional(p_desde_in);
        p_hasta := fecha_opcional(p_hasta_in);
        -- Texto AAAA-MM-DD: el orden alfabetico es el orden de las fechas.
        IF p_desde IS NOT NULL AND p_hasta IS NOT NULL AND p_hasta < p_desde THEN
            RAISE_APPLICATION_ERROR(-20080, 'La fecha final no puede ser anterior a la inicial.');
        END IF;
    END normalizar_rango;

    FUNCTION fn_estadisticas_generales (p_actor_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cur SYS_REFCURSOR;
        v_hoy VARCHAR2(10) := TO_CHAR(TRUNC(SYSDATE), 'YYYY-MM-DD');
    BEGIN
        sp_exigir_admin(p_actor_id);
        -- Cada habitacion cae en UNA sola clase, en este orden de prioridad:
        -- mantenimiento > no disponible > ocupada hoy (CHECKED_IN) > reservada hoy (ACTIVE) > libre.
        OPEN v_cur FOR
            SELECT h.total_habitaciones,
                   h.ocupadas,
                   h.reservadas,
                   h.libres,
                   h.mantenimiento,
                   h.no_disponibles,
                   i.ingresos,
                   s.saldo_pendiente
              FROM (
                    SELECT COUNT(*) AS total_habitaciones,
                           COUNT(CASE WHEN c.clase = 'OCUPADA'       THEN 1 END) AS ocupadas,
                           COUNT(CASE WHEN c.clase = 'RESERVADA'     THEN 1 END) AS reservadas,
                           COUNT(CASE WHEN c.clase = 'LIBRE'         THEN 1 END) AS libres,
                           COUNT(CASE WHEN c.clase = 'MANTENIMIENTO' THEN 1 END) AS mantenimiento,
                           COUNT(CASE WHEN c.clase = 'NO_DISPONIBLE' THEN 1 END) AS no_disponibles
                      FROM (
                            SELECT CASE
                                     WHEN h.en_mantenimiento = 1 THEN 'MANTENIMIENTO'
                                     WHEN h.disponible = 0       THEN 'NO_DISPONIBLE'
                                     WHEN EXISTS (SELECT 1 FROM reservas r
                                                   WHERE r.habitacion_id = h.id
                                                     AND r.estado = 'CHECKED_IN'
                                                     AND r.entrada <= v_hoy AND r.salida > v_hoy) THEN 'OCUPADA'
                                     WHEN EXISTS (SELECT 1 FROM reservas r
                                                   WHERE r.habitacion_id = h.id
                                                     AND r.estado IN ('ACTIVE', 'CHECKED_IN')
                                                     AND r.entrada <= v_hoy AND r.salida > v_hoy) THEN 'RESERVADA'
                                     ELSE 'LIBRE'
                                   END AS clase
                              FROM habitaciones h
                           ) c
                   ) h
              CROSS JOIN (
                    SELECT NVL(SUM(cp.pagado - cp.reembolso), 0) AS ingresos
                      FROM comprobantes cp
                   ) i
              CROSS JOIN (
                    SELECT NVL(SUM(CASE WHEN cp.estado IN ('PENDING', 'PARTIALLY_PAID')
                                        THEN cp.monto - cp.pagado ELSE 0 END), 0) AS saldo_pendiente
                      FROM comprobantes cp
                   ) s;
        RETURN v_cur;
    END fn_estadisticas_generales;

    FUNCTION fn_reservas_por_estado (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR IS
        v_cur   SYS_REFCURSOR;
        v_desde VARCHAR2(10);
        v_hasta VARCHAR2(10);
    BEGIN
        sp_exigir_admin(p_actor_id);
        normalizar_rango(p_desde, p_hasta, v_desde, v_hasta);
        OPEN v_cur FOR
            SELECT e.estado,
                   COUNT(r.id)           AS cantidad,
                   NVL(SUM(r.total), 0)  AS valor_total
              FROM (SELECT 'ACTIVE'      AS estado, 1 AS orden FROM dual
                    UNION ALL SELECT 'CHECKED_IN',  2 FROM dual
                    UNION ALL SELECT 'CHECKED_OUT', 3 FROM dual
                    UNION ALL SELECT 'CANCELLED',   4 FROM dual) e
              LEFT JOIN reservas r
                     ON r.estado = e.estado
                    AND (v_desde IS NULL OR r.entrada >= v_desde)
                    AND (v_hasta IS NULL OR r.entrada <= v_hasta)
             GROUP BY e.estado, e.orden
             ORDER BY e.orden;
        RETURN v_cur;
    END fn_reservas_por_estado;

    FUNCTION fn_ingresos_por_mes (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR IS
        v_cur   SYS_REFCURSOR;
        v_desde VARCHAR2(10);
        v_hasta VARCHAR2(10);
    BEGIN
        sp_exigir_admin(p_actor_id);
        normalizar_rango(p_desde, p_hasta, v_desde, v_hasta);
        OPEN v_cur FOR
            SELECT SUBSTR(cp.fecha_emision, 1, 7)  AS mes,
                   COUNT(*)                        AS comprobantes,
                   SUM(cp.pagado)                  AS cobrado,
                   SUM(cp.reembolso)               AS reembolsado,
                   SUM(cp.pagado - cp.reembolso)   AS neto
              FROM comprobantes cp
             WHERE (v_desde IS NULL OR cp.fecha_emision >= v_desde)
               AND (v_hasta IS NULL OR cp.fecha_emision <= v_hasta)
             GROUP BY SUBSTR(cp.fecha_emision, 1, 7)
             ORDER BY mes DESC;
        RETURN v_cur;
    END fn_ingresos_por_mes;

    FUNCTION fn_uso_habitaciones (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR IS
        v_cur   SYS_REFCURSOR;
        v_desde VARCHAR2(10);
        v_hasta VARCHAR2(10);
    BEGIN
        sp_exigir_admin(p_actor_id);
        normalizar_rango(p_desde, p_hasta, v_desde, v_hasta);
        OPEN v_cur FOR
            SELECT h.numero,
                   h.tipo,
                   COUNT(r.id)                                 AS veces,
                   NVL(SUM(fn_noches(r.entrada, r.salida)), 0) AS noches,
                   NVL(SUM(r.total), 0)                        AS valor
              FROM habitaciones h
              LEFT JOIN reservas r
                     ON r.habitacion_id = h.id
                    AND r.estado <> 'CANCELLED'
                    AND (v_desde IS NULL OR r.entrada >= v_desde)
                    AND (v_hasta IS NULL OR r.entrada <= v_hasta)
             GROUP BY h.id, h.numero, h.tipo
             ORDER BY veces DESC, noches DESC, h.numero;
        RETURN v_cur;
    END fn_uso_habitaciones;

    FUNCTION fn_mejores_clientes (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR IS
        v_cur   SYS_REFCURSOR;
        v_desde VARCHAR2(10);
        v_hasta VARCHAR2(10);
    BEGIN
        sp_exigir_admin(p_actor_id);
        normalizar_rango(p_desde, p_hasta, v_desde, v_hasta);
        OPEN v_cur FOR
            SELECT t.nombre, t.categoria, t.cant_reservas, t.noches, t.valor
              FROM (SELECT c.nombre,
                           c.categoria,
                           COUNT(r.id)                        AS cant_reservas,
                           SUM(fn_noches(r.entrada, r.salida)) AS noches,
                           SUM(r.total)                       AS valor
                      FROM reservas r
                      JOIN clientes c ON c.id = r.cliente_id
                     WHERE r.estado <> 'CANCELLED'
                       AND (v_desde IS NULL OR r.entrada >= v_desde)
                       AND (v_hasta IS NULL OR r.entrada <= v_hasta)
                     GROUP BY c.id, c.nombre, c.categoria
                     ORDER BY valor DESC, c.nombre, c.id) t
             WHERE ROWNUM <= 10;
        RETURN v_cur;
    END fn_mejores_clientes;

    FUNCTION fn_saldos_pendientes (
        p_actor_id IN NUMBER,
        p_desde    IN VARCHAR2 DEFAULT NULL,
        p_hasta    IN VARCHAR2 DEFAULT NULL
    ) RETURN SYS_REFCURSOR IS
        v_cur   SYS_REFCURSOR;
        v_desde VARCHAR2(10);
        v_hasta VARCHAR2(10);
    BEGIN
        sp_exigir_admin(p_actor_id);
        normalizar_rango(p_desde, p_hasta, v_desde, v_hasta);
        OPEN v_cur FOR
            SELECT cp.id                              AS comprobante_id,
                   c.nombre                           AS cliente,
                   cp.fecha_emision,
                   cp.monto,
                   cp.pagado,
                   GREATEST(cp.monto - cp.pagado, 0)  AS saldo,
                   cp.estado
              FROM comprobantes cp
              JOIN reservas r ON r.id = cp.reserva_id
              JOIN clientes c ON c.id = r.cliente_id
             WHERE cp.estado IN ('PENDING', 'PARTIALLY_PAID')
               AND (v_desde IS NULL OR cp.fecha_emision >= v_desde)
               AND (v_hasta IS NULL OR cp.fecha_emision <= v_hasta)
             ORDER BY cp.id;
        RETURN v_cur;
    END fn_saldos_pendientes;

    FUNCTION fn_top_habitaciones (
        p_desde  IN VARCHAR2 DEFAULT NULL,
        p_limite IN NUMBER   DEFAULT NULL
    ) RETURN SYS_REFCURSOR IS
        v_cur   SYS_REFCURSOR;
        v_desde VARCHAR2(10) := fecha_opcional(p_desde);
    BEGIN
        IF p_limite IS NOT NULL AND p_limite < 1 THEN
            RAISE_APPLICATION_ERROR(-20060, 'El limite del ranking debe ser mayor que cero.');
        END IF;
        OPEN v_cur FOR
            SELECT t.id, t.numero, t.tipo, t.veces, t.noches
              FROM (SELECT h.id,
                           h.numero,
                           h.tipo,
                           COUNT(r.id)                                 AS veces,
                           NVL(SUM(fn_noches(r.entrada, r.salida)), 0) AS noches
                      FROM habitaciones h
                      LEFT JOIN reservas r
                             ON r.habitacion_id = h.id
                            AND r.estado <> 'CANCELLED'
                            AND (v_desde IS NULL OR r.entrada >= v_desde)
                     GROUP BY h.id, h.numero, h.tipo
                     ORDER BY veces DESC, noches DESC, h.numero) t
             WHERE p_limite IS NULL OR ROWNUM <= p_limite;
        RETURN v_cur;
    END fn_top_habitaciones;

END pkg_reportes;
/

-- 10. INDICES PARA LAS CONSULTAS Y RELACIONES
CREATE INDEX ix_reservas_cliente ON reservas (cliente_id);
CREATE INDEX ix_reservas_trabajador ON reservas (trabajador_id);
CREATE INDEX ix_reservas_habitacion ON reservas (habitacion_id, estado);
CREATE INDEX ix_comprobantes_reserva ON comprobantes (reserva_id);
CREATE INDEX ix_auditoria_reserva ON auditoria_reservas (reserva_id);

-- 11. VISTAS DE CONSULTA
CREATE OR REPLACE VIEW v_reservas_activas AS
SELECT r.id, c.nombre AS cliente, h.numero AS habitacion, r.entrada, r.salida,
       r.huespedes, r.total, r.estado
  FROM reservas r
  JOIN clientes c ON c.id = r.cliente_id
  JOIN habitaciones h ON h.id = r.habitacion_id
 WHERE r.estado IN ('ACTIVE', 'CHECKED_IN');

CREATE OR REPLACE VIEW v_habitaciones_libres_hoy AS
SELECT h.id, h.numero, h.tipo, h.capacidad, h.precio_noche
  FROM habitaciones h
 WHERE h.disponible = 1
   AND h.en_mantenimiento = 0
   AND NOT EXISTS (
       SELECT 1 FROM reservas r
        WHERE r.habitacion_id = h.id
          AND r.estado IN ('ACTIVE', 'CHECKED_IN')
          AND TO_DATE(r.entrada, 'YYYY-MM-DD') <= TRUNC(SYSDATE)
          AND TO_DATE(r.salida, 'YYYY-MM-DD') > TRUNC(SYSDATE));

CREATE OR REPLACE VIEW v_ingresos_mensuales AS
SELECT SUBSTR(fecha_emision, 1, 7) AS mes,
       COUNT(*) AS comprobantes,
       SUM(pagado - reembolso) AS ingresos,
       SUM(CASE WHEN estado IN ('PENDING', 'PARTIALLY_PAID') THEN monto - pagado ELSE 0 END) AS saldo_pendiente
  FROM comprobantes
 GROUP BY SUBSTR(fecha_emision, 1, 7);

COMMIT;

-- Fin del esquema HotelReservas.
