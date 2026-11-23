-- 02_funciones.sql
-- Funciones que usan los triggers de 03_triggers.sql.
--
-- ORDEN: despues de 01_crear_tablas.sql (o cuando las 5 tablas ya existan) y antes de 03_triggers.sql.
-- Se puede ejecutar varias veces (CREATE OR REPLACE).
-- Las fechas son texto AAAA-MM-DD, igual que las guarda el programa Java.

-- Noches entre dos fechas (negativo o cero si la salida no es posterior a la entrada).
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
