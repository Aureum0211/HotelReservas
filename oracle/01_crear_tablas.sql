-- 01_crear_tablas.sql
-- Crea las 5 tablas SOLO si no existen (ORA-00955 se ignora). Es el mismo esquema que antes creaba
-- Database.java al conectarse; ahora el esquema vive solo en Oracle.
-- Si ya tienes las tablas (tu caso), este script no cambia nada.
-- Instalacion nueva: ejecuta los scripts 01 al 10 en orden.
SET SERVEROUTPUT ON

DECLARE
    PROCEDURE crear(p_tabla IN VARCHAR2, p_sql IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
        DBMS_OUTPUT.PUT_LINE('tabla ' || p_tabla || ': creada');
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -955 THEN RAISE; END IF;
            DBMS_OUTPUT.PUT_LINE('tabla ' || p_tabla || ': ya existia');
    END crear;
BEGIN
    crear('clientes', q'[CREATE TABLE clientes (
        id NUMBER(10) CONSTRAINT pk_clientes PRIMARY KEY,
        nombre VARCHAR2(60 CHAR) NOT NULL,
        documento VARCHAR2(15 CHAR) NOT NULL,
        correo VARCHAR2(50 CHAR) NOT NULL,
        telefono VARCHAR2(10) NOT NULL,
        ciudad VARCHAR2(40 CHAR) NOT NULL,
        categoria VARCHAR2(8) NOT NULL CONSTRAINT ck_clientes_categoria CHECK (categoria IN ('NEW', 'FREQUENT', 'VIP')),
        activo NUMBER(1) DEFAULT 1 NOT NULL CONSTRAINT ck_clientes_activo CHECK (activo IN (0, 1))
    )]');

    crear('trabajadores', q'[CREATE TABLE trabajadores (
        id NUMBER(10) CONSTRAINT pk_trabajadores PRIMARY KEY,
        nombre VARCHAR2(60 CHAR) NOT NULL,
        documento VARCHAR2(15 CHAR) NOT NULL,
        correo VARCHAR2(50 CHAR) NOT NULL,
        telefono VARCHAR2(10) NOT NULL,
        ciudad VARCHAR2(40 CHAR) NOT NULL,
        activo NUMBER(1) DEFAULT 1 NOT NULL CONSTRAINT ck_trabajadores_activo CHECK (activo IN (0, 1)),
        rol VARCHAR2(12) NOT NULL CONSTRAINT ck_trabajadores_rol CHECK (rol IN ('RECEPTIONIST', 'ADMIN')),
        usuario VARCHAR2(20 CHAR) NOT NULL CONSTRAINT uq_trabajadores_usuario UNIQUE,
        contrasena VARCHAR2(30 CHAR) NOT NULL
    )]');

    crear('habitaciones', q'[CREATE TABLE habitaciones (
        id NUMBER(10) CONSTRAINT pk_habitaciones PRIMARY KEY,
        numero VARCHAR2(6 CHAR) NOT NULL CONSTRAINT uq_habitaciones_numero UNIQUE,
        tipo VARCHAR2(6) NOT NULL CONSTRAINT ck_habitaciones_tipo CHECK (tipo IN ('SINGLE', 'DOUBLE', 'SUITE')),
        capacidad NUMBER(2) NOT NULL CONSTRAINT ck_habitaciones_capacidad CHECK (capacidad > 0),
        precio_noche NUMBER(10,2) NOT NULL CONSTRAINT ck_habitaciones_precio CHECK (precio_noche > 0),
        disponible NUMBER(1) DEFAULT 1 NOT NULL CONSTRAINT ck_habitaciones_disponible CHECK (disponible IN (0, 1)),
        en_mantenimiento NUMBER(1) DEFAULT 0 NOT NULL CONSTRAINT ck_habitaciones_mant CHECK (en_mantenimiento IN (0, 1))
    )]');

    crear('reservas', q'[CREATE TABLE reservas (
        id NUMBER(10) CONSTRAINT pk_reservas PRIMARY KEY,
        cliente_id NUMBER(10) NOT NULL CONSTRAINT fk_reservas_cliente REFERENCES clientes(id),
        trabajador_id NUMBER(10) CONSTRAINT fk_reservas_trabajador REFERENCES trabajadores(id),
        habitacion_id NUMBER(10) NOT NULL CONSTRAINT fk_reservas_habitacion REFERENCES habitaciones(id),
        entrada VARCHAR2(10) NOT NULL,
        salida VARCHAR2(10) NOT NULL,
        precio_noche NUMBER(10,2) NOT NULL,
        total NUMBER(12,2) NOT NULL,
        huespedes NUMBER(2) NOT NULL CONSTRAINT ck_reservas_huespedes CHECK (huespedes > 0),
        estado VARCHAR2(11) NOT NULL CONSTRAINT ck_reservas_estado CHECK (estado IN ('ACTIVE', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'))
    )]');

    crear('comprobantes', q'[CREATE TABLE comprobantes (
        id NUMBER(10) CONSTRAINT pk_comprobantes PRIMARY KEY,
        reserva_id NUMBER(10) NOT NULL CONSTRAINT fk_comprobantes_reserva REFERENCES reservas(id),
        fecha_emision VARCHAR2(10) NOT NULL,
        monto NUMBER(12,2) NOT NULL,
        metodo_pago VARCHAR2(8) NOT NULL CONSTRAINT ck_comprobantes_metodo CHECK (metodo_pago IN ('CASH', 'CARD', 'TRANSFER')),
        estado VARCHAR2(14) NOT NULL CONSTRAINT ck_comprobantes_estado CHECK (estado IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'REFUNDED', 'VOIDED')),
        reembolso NUMBER(12,2) DEFAULT 0 NOT NULL,
        pagado NUMBER(12,2) DEFAULT 0 NOT NULL
    )]');
END;
/
