-- 09_mejoras_bd.sql
-- Mejoras minimas para cerrar la base de datos: restricciones que faltan, indices, vistas y roles.
-- ORDEN: ejecutar despues de instalar los paquetes (scripts 05 al 08). Se puede ejecutar varias veces.
-- Ejecutalo como SCRIPT (F5). Cada paso que falle (por ejemplo, un dato existente que rompe una
-- restriccion, o un permiso que falta) muestra un aviso y NO detiene el resto.
SET SERVEROUTPUT ON

DECLARE
    PROCEDURE ejecutar(p_nombre IN VARCHAR2, p_sql IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
        DBMS_OUTPUT.PUT_LINE('ok      ' || p_nombre);
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE IN (-2264, -2261, -955) THEN
                DBMS_OUTPUT.PUT_LINE('ya existe ' || p_nombre);
            ELSE
                DBMS_OUTPUT.PUT_LINE('AVISO   ' || p_nombre || ' -> ' || SQLERRM);
            END IF;
    END ejecutar;
BEGIN
    -- 1) Unicidad: la misma persona no puede registrarse dos veces.
    ejecutar('uq_clientes_documento',
             'ALTER TABLE clientes ADD CONSTRAINT uq_clientes_documento UNIQUE (documento)');
    ejecutar('uq_clientes_correo',
             'ALTER TABLE clientes ADD CONSTRAINT uq_clientes_correo UNIQUE (correo)');
    ejecutar('uq_trabajadores_documento',
             'ALTER TABLE trabajadores ADD CONSTRAINT uq_trabajadores_documento UNIQUE (documento)');
    ejecutar('uq_trabajadores_correo',
             'ALTER TABLE trabajadores ADD CONSTRAINT uq_trabajadores_correo UNIQUE (correo)');

    -- 2) Valores coherentes en reservas y comprobantes.
    ejecutar('ck_reservas_precio',
             'ALTER TABLE reservas ADD CONSTRAINT ck_reservas_precio CHECK (precio_noche > 0)');
    ejecutar('ck_reservas_total',
             'ALTER TABLE reservas ADD CONSTRAINT ck_reservas_total CHECK (total > 0)');
    ejecutar('ck_comprobantes_monto',
             'ALTER TABLE comprobantes ADD CONSTRAINT ck_comprobantes_monto CHECK (monto > 0)');
    ejecutar('ck_comprobantes_pagado',
             'ALTER TABLE comprobantes ADD CONSTRAINT ck_comprobantes_pagado CHECK (pagado >= 0 AND pagado <= monto)');
    ejecutar('ck_comprobantes_reembolso',
             'ALTER TABLE comprobantes ADD CONSTRAINT ck_comprobantes_reembolso CHECK (reembolso >= 0 AND reembolso <= monto)');

    -- 3) Indices en las llaves foraneas (Oracle no los crea solo) y en la busqueda de cruces.
    ejecutar('ix_reservas_cliente',
             'CREATE INDEX ix_reservas_cliente ON reservas (cliente_id)');
    ejecutar('ix_reservas_trabajador',
             'CREATE INDEX ix_reservas_trabajador ON reservas (trabajador_id)');
    ejecutar('ix_reservas_habitacion',
             'CREATE INDEX ix_reservas_habitacion ON reservas (habitacion_id, estado)');
    ejecutar('ix_comprobantes_reserva',
             'CREATE INDEX ix_comprobantes_reserva ON comprobantes (reserva_id)');
    ejecutar('ix_auditoria_reserva',
             'CREATE INDEX ix_auditoria_reserva ON auditoria_reservas (reserva_id)');

    -- 4) Vistas de consulta.
    ejecutar('v_reservas_activas', q'[
        CREATE OR REPLACE VIEW v_reservas_activas AS
        SELECT r.id, c.nombre AS cliente, h.numero AS habitacion, r.entrada, r.salida,
               r.huespedes, r.total, r.estado
          FROM reservas r
          JOIN clientes c     ON c.id = r.cliente_id
          JOIN habitaciones h ON h.id = r.habitacion_id
         WHERE r.estado IN ('ACTIVE', 'CHECKED_IN')]');

    ejecutar('v_habitaciones_libres_hoy', q'[
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
                    AND TO_DATE(r.salida,  'YYYY-MM-DD') >  TRUNC(SYSDATE))]');

    -- Mismo calculo que el reporte de Java: abonado menos reembolsado.
    ejecutar('v_ingresos_mensuales', q'[
        CREATE OR REPLACE VIEW v_ingresos_mensuales AS
        SELECT SUBSTR(fecha_emision, 1, 7) AS mes,
               COUNT(*)                    AS comprobantes,
               SUM(pagado - reembolso)     AS ingresos,
               SUM(CASE WHEN estado IN ('PENDING', 'PARTIALLY_PAID') THEN monto - pagado ELSE 0 END) AS saldo_pendiente
          FROM comprobantes
         GROUP BY SUBSTR(fecha_emision, 1, 7)]');

    -- 5) Roles (necesitan el privilegio CREATE ROLE; si no lo tienes, saldra un AVISO y puedes omitirlo).
    ejecutar('rol_recepcionista', 'CREATE ROLE rol_recepcionista');
    ejecutar('rol_administrador', 'CREATE ROLE rol_administrador');
    ejecutar('permisos recepcionista 1', 'GRANT SELECT ON clientes TO rol_recepcionista');
    ejecutar('permisos recepcionista 2', 'GRANT SELECT ON habitaciones TO rol_recepcionista');
    ejecutar('permisos recepcionista 3', 'GRANT SELECT ON v_reservas_activas TO rol_recepcionista');
    ejecutar('permisos recepcionista 4', 'GRANT SELECT ON v_habitaciones_libres_hoy TO rol_recepcionista');
    ejecutar('permisos recepcionista 5', 'GRANT EXECUTE ON pkg_reservas TO rol_recepcionista');
    ejecutar('permisos administrador 1', 'GRANT rol_recepcionista TO rol_administrador');
    ejecutar('permisos administrador 2', 'GRANT SELECT ON trabajadores TO rol_administrador');
    ejecutar('permisos administrador 3', 'GRANT SELECT ON v_ingresos_mensuales TO rol_administrador');
    ejecutar('permisos administrador 4', 'GRANT SELECT ON auditoria_reservas TO rol_administrador');
END;
/

-- Comprobacion: restricciones nuevas, indices y vistas.
SELECT constraint_name, table_name, status FROM user_constraints
 WHERE constraint_name IN ('UQ_CLIENTES_DOCUMENTO', 'UQ_CLIENTES_CORREO', 'UQ_TRABAJADORES_DOCUMENTO',
       'UQ_TRABAJADORES_CORREO', 'CK_RESERVAS_PRECIO', 'CK_RESERVAS_TOTAL', 'CK_COMPROBANTES_MONTO',
       'CK_COMPROBANTES_PAGADO', 'CK_COMPROBANTES_REEMBOLSO')
 ORDER BY table_name, constraint_name;

SELECT object_type, object_name, status FROM user_objects
 WHERE object_type IN ('VIEW', 'INDEX') AND (object_name LIKE 'V\_%' ESCAPE '\' OR object_name LIKE 'IX\_%' ESCAPE '\')
 ORDER BY object_type, object_name;
