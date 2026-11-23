-- 06_paquete_personas.sql
-- Paquete PKG_PERSONAS: registro de clientes, huespedes y trabajadores, e inicio de sesion.
-- Reemplaza las reglas que estaban en Java (HotelService.registerClient/registerGuest/registerEmployee/
-- login, ensurePersonIsUnique y Validator.requireText/requireEmail/requirePhone).
-- ORDEN: despues de 04_base_comun.sql. Se puede ejecutar varias veces. Ejecutalo como SCRIPT (F5).
--
-- Contrasenas: se conservan en texto normal, como hasta ahora (no se agrega cifrado).
--
-- Codigos de error:
--   -20060 dato obligatorio o demasiado largo   -20065 telefono con formato invalido
--   -20061 documento ya registrado              -20066 el usuario ya existe
--   -20062 correo ya registrado                 -20067 categoria o rol no validos
--   -20063 telefono ya registrado               -20068 usuario o contrasena incorrectos
--   -20064 correo con formato invalido          -20069 no hay un cliente con ese documento
--   (-20050 / -20051: sp_exigir_admin)
SET SERVEROUTPUT ON

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

-- Comprobacion: ambos deben decir VALID, y la segunda consulta no debe devolver filas.
SELECT object_type, object_name, status FROM user_objects
 WHERE object_name = 'PKG_PERSONAS' ORDER BY object_type;

SELECT name, type, line, text FROM user_errors WHERE name = 'PKG_PERSONAS' ORDER BY type, sequence;
