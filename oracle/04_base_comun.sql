-- 04_base_comun.sql
-- Piezas compartidas por todos los paquetes:
--   * secuencias para los ids de cada tabla (empiezan despues del mayor id existente);
--   * los 4 trabajadores iniciales (antes los sembraba Database.java) SOLO si la tabla esta vacia;
--   * fn_siguiente_id   : toma el siguiente id de una secuencia saltando ids ya usados;
--   * fn_rol_trabajador : rol de un trabajador ACTIVO (NULL si no existe o esta inactivo);
--   * sp_exigir_admin   : error si quien actua no es un administrador activo.
-- ORDEN: despues de 01_crear_tablas.sql, 02_funciones.sql y 03_triggers.sql; antes de 05 al 08.
-- Ejecutalo como SCRIPT (F5).
SET SERVEROUTPUT ON

DECLARE
    PROCEDURE crear_seq(p_seq IN VARCHAR2, p_tabla IN VARCHAR2) IS
        v_inicio NUMBER;
    BEGIN
        EXECUTE IMMEDIATE 'SELECT NVL(MAX(id), 0) + 1 FROM ' || p_tabla INTO v_inicio;
        EXECUTE IMMEDIATE 'CREATE SEQUENCE ' || p_seq || ' START WITH ' || v_inicio
                          || ' INCREMENT BY 1 NOCACHE';
        DBMS_OUTPUT.PUT_LINE('secuencia ' || p_seq || ': creada (empieza en ' || v_inicio || ')');
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE != -955 THEN RAISE; END IF;
            DBMS_OUTPUT.PUT_LINE('secuencia ' || p_seq || ': ya existia');
    END crear_seq;
BEGIN
    crear_seq('seq_clientes', 'clientes');
    crear_seq('seq_trabajadores', 'trabajadores');
    crear_seq('seq_habitaciones', 'habitaciones');
    crear_seq('seq_reservas', 'reservas');
    crear_seq('seq_comprobantes', 'comprobantes');
END;
/

-- Trabajadores iniciales: solo si la tabla esta vacia (instalacion nueva).
DECLARE
    v_n NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_n FROM trabajadores;
    IF v_n = 0 THEN
        INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
        VALUES (1, 'Administrador', '1000000001', 'admin@hotel.com', '3001234567', 'Valledupar', 1, 'ADMIN', 'admin', 'admin123');
        INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
        VALUES (2, 'Laura Gomez', '1000000002', 'laura@hotel.com', '3002345678', 'Valledupar', 1, 'RECEPTIONIST', 'recepcion1', 'recep123');
        INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
        VALUES (3, 'Carlos Perez', '1000000003', 'carlos@hotel.com', '3003456789', 'Valledupar', 1, 'RECEPTIONIST', 'recepcion2', 'recep456');
        INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
        VALUES (4, 'Ana Torres', '1000000004', 'ana@hotel.com', '3004567890', 'Valledupar', 0, 'RECEPTIONIST', 'inactivo', 'inactivo123');
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('trabajadores iniciales: insertados');
    ELSE
        DBMS_OUTPUT.PUT_LINE('trabajadores iniciales: la tabla ya tiene datos, no se toca');
    END IF;
END;
/

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

-- Comprobacion: todo debe decir VALID y la segunda consulta no debe devolver filas.
SELECT object_type, object_name, status FROM user_objects
 WHERE object_name IN ('FN_SIGUIENTE_ID', 'FN_ROL_TRABAJADOR', 'SP_EXIGIR_ADMIN') ORDER BY object_name;
SELECT name, line, text FROM user_errors
 WHERE name IN ('FN_SIGUIENTE_ID', 'FN_ROL_TRABAJADOR', 'SP_EXIGIR_ADMIN') ORDER BY name, sequence;
