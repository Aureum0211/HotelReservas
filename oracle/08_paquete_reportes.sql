-- 08_paquete_reportes.sql
-- Paquete PKG_REPORTES: estadisticas generales, reportes y ranking de habitaciones.
-- Reemplaza los calculos que estaban en Java: HotelService.getBasicReport, ui.Reportes (5 reportes)
-- y el ranking que armaba TopHabitacionesController.
-- ORDEN: despues de 04_base_comun.sql y 02_funciones.sql. Se puede ejecutar varias veces.
-- Ejecutalo como SCRIPT (F5).
--
-- Cada funcion devuelve un SYS_REFCURSOR con las cifras YA calculadas (conteos, sumas, orden y limites).
-- Java solo le da formato (moneda, fechas, nombres en espanol) y lo muestra.
--
-- Fechas: texto AAAA-MM-DD, igual que en las tablas. p_desde / p_hasta son opcionales (NULL = sin limite)
-- y filtran por la fecha de ENTRADA de la reserva o por la fecha de EMISION del comprobante.
--
-- Permisos: todo requiere un ADMIN activo (p_actor_id), EXCEPTO fn_top_habitaciones, que la pantalla
-- principal consulta tambien para los recepcionistas.
--
-- Codigos de error:
--   -20080 la fecha final es anterior a la inicial
--   -20004 fecha con formato invalido (viene de fn_noches; se reutiliza el mismo codigo)
--   -20050 / -20051 permisos (vienen de sp_exigir_admin)
SET SERVEROUTPUT ON

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

-- Comprobacion: ambos deben decir VALID, y la segunda consulta no debe devolver filas.
SELECT object_type, object_name, status FROM user_objects
 WHERE object_name = 'PKG_REPORTES' ORDER BY object_type;

SELECT name, type, line, text FROM user_errors WHERE name = 'PKG_REPORTES' ORDER BY type, sequence;
