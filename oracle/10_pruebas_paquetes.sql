-- 10_pruebas_paquetes.sql
-- Prueba PKG_PERSONAS, PKG_HABITACIONES, PKG_RESERVAS (version 3) y PKG_REPORTES con datos TEMPORALES
-- (ids 900001+) y hace ROLLBACK al final: no deja nada guardado.
-- Incluye pruebas integradas de personas, habitaciones, reservas y reportes.
-- Ejecutalo como SCRIPT (F5) despues de los scripts 01 al 09. Resultado esperado: 0 fallan.
-- Si ves "ORA-00001" al inicio es que sobraron datos de una prueba anterior que no hizo ROLLBACK:
-- ejecuta ROLLBACK; y vuelve a intentarlo.
SET SERVEROUTPUT ON

DECLARE
    v_pasan  NUMBER := 0;
    v_fallan NUMBER := 0;

    v_hoy    VARCHAR2(10) := TO_CHAR(SYSDATE,      'YYYY-MM-DD');
    v_d3     VARCHAR2(10) := TO_CHAR(SYSDATE + 3,  'YYYY-MM-DD');
    v_d5     VARCHAR2(10) := TO_CHAR(SYSDATE + 5,  'YYYY-MM-DD');
    v_d8     VARCHAR2(10) := TO_CHAR(SYSDATE + 8,  'YYYY-MM-DD');
    v_d10    VARCHAR2(10) := TO_CHAR(SYSDATE + 10, 'YYYY-MM-DD');
    v_d12    VARCHAR2(10) := TO_CHAR(SYSDATE + 12, 'YYYY-MM-DD');
    v_d20    VARCHAR2(10) := TO_CHAR(SYSDATE + 20, 'YYYY-MM-DD');
    v_d22    VARCHAR2(10) := TO_CHAR(SYSDATE + 22, 'YYYY-MM-DD');
    v_d30    VARCHAR2(10) := TO_CHAR(SYSDATE + 30, 'YYYY-MM-DD');
    v_d31    VARCHAR2(10) := TO_CHAR(SYSDATE + 31, 'YYYY-MM-DD');
    v_ayer2  VARCHAR2(10) := TO_CHAR(SYSDATE - 2,  'YYYY-MM-DD');

    v_id     NUMBER;   -- id devuelto por un procedimiento
    v_texto  VARCHAR2(200);
    v_num    NUMBER;
    v_num2   NUMBER;
    v_resA   NUMBER;   -- reserva A: hoy -> hoy+5, habitacion 900001, con trabajador
    v_resB   NUMBER;   -- reserva B: hoy+10 -> hoy+12, habitacion 900001, sin trabajador
    v_resC   NUMBER;   -- reserva C: hoy+20 -> hoy+22, habitacion 900002, cliente VIP
    v_resD   NUMBER;   -- reserva D: hoy+30 -> hoy+31, habitacion 900002, cliente VIP
    v_compA  NUMBER;
    v_compB  NUMBER;
    v_compC  NUMBER;
    v_hab    NUMBER;

    PROCEDURE registrar(p_ok IN BOOLEAN, p_nombre IN VARCHAR2, p_detalle IN VARCHAR2 DEFAULT NULL) IS
    BEGIN
        IF p_ok THEN
            v_pasan := v_pasan + 1;
            DBMS_OUTPUT.PUT_LINE('ok     ' || p_nombre);
        ELSE
            v_fallan := v_fallan + 1;
            DBMS_OUTPUT.PUT_LINE('FALLA  ' || p_nombre || ' ' || p_detalle);
        END IF;
    END registrar;

    -- La sentencia debe terminar con el error indicado (por ejemplo -20003).
    PROCEDURE debe_fallar(p_nombre IN VARCHAR2, p_sql IN VARCHAR2, p_codigo IN NUMBER) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
        registrar(FALSE, p_nombre, '(no dio error; se esperaba ' || p_codigo || ')');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(SQLCODE = p_codigo, p_nombre, '(llego ' || SQLERRM || ')');
    END debe_fallar;

    -- La sentencia debe ejecutarse sin error.
    PROCEDURE debe_funcionar(p_nombre IN VARCHAR2, p_sql IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
        registrar(TRUE, p_nombre);
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, p_nombre, '(llego ' || SQLERRM || ')');
    END debe_funcionar;
BEGIN
    -- ------------------------------------------------------------------ datos de apoyo temporales
    INSERT INTO clientes (id, nombre, documento, correo, telefono, ciudad, categoria, activo)
    VALUES (900001, 'Cliente Prueba', '900000001', 'prueba900001@test.com', '9000000001', 'Prueba', 'NEW', 1);
    INSERT INTO clientes (id, nombre, documento, correo, telefono, ciudad, categoria, activo)
    VALUES (900002, 'Cliente Vip Prueba', '900000005', 'prueba900002@test.com', '9000000005', 'Prueba', 'VIP', 1);

    INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
    VALUES (900001, 'Admin Prueba', '900000002', 'trab900001@test.com', '9000000002', 'Prueba', 1, 'ADMIN', 'prueba900001', 'x');
    INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
    VALUES (900002, 'Recepcion Prueba', '900000003', 'trab900002@test.com', '9000000003', 'Prueba', 1, 'RECEPTIONIST', 'prueba900002', 'y');
    INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)
    VALUES (900003, 'Inactivo Prueba', '900000004', 'trab900003@test.com', '9000000004', 'Prueba', 0, 'ADMIN', 'prueba900003', 'z');

    INSERT INTO habitaciones (id, numero, tipo, capacidad, precio_noche, disponible, en_mantenimiento)
    VALUES (900001, 'T-901', 'SINGLE', 2, 100000, 1, 0);
    INSERT INTO habitaciones (id, numero, tipo, capacidad, precio_noche, disponible, en_mantenimiento)
    VALUES (900002, 'T-902', 'SUITE', 4, 200000, 1, 0);

    -- ================================================================== PKG_PERSONAS
    BEGIN
        pkg_personas.sp_registrar_cliente('  Persona Uno  ', '900000011', 'Persona.Uno@Test.COM', '9000000011', 'Valledupar', 'FREQUENT', v_id);
        SELECT nombre || '|' || correo || '|' || categoria INTO v_texto FROM clientes WHERE id = v_id;
        registrar(v_texto = 'Persona Uno|persona.uno@test.com|FREQUENT',
                  'registrar cliente: recorta el nombre y guarda el correo en minusculas', '(quedo ' || v_texto || ')');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'registrar cliente valido', '(llego ' || SQLERRM || ')');
    END;

    debe_fallar('documento repetido entre clientes',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000011','otro1@test.com','9000000012','Ciudad','NEW',x); END;]', -20061);
    debe_fallar('documento repetido con un TRABAJADOR (se revisan ambas tablas)',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000002','otro2@test.com','9000000013','Ciudad','NEW',x); END;]', -20061);
    debe_fallar('correo repetido (sin distinguir mayusculas)',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000014','PERSONA.UNO@test.com','9000000014','Ciudad','NEW',x); END;]', -20062);
    debe_fallar('telefono repetido con un trabajador',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000015','otro3@test.com','9000000002','Ciudad','NEW',x); END;]', -20063);
    debe_fallar('correo con formato invalido',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000016','sin-arroba.com','9000000016','Ciudad','NEW',x); END;]', -20064);
    debe_fallar('telefono con menos de 10 digitos',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000017','otro4@test.com','900000017','Ciudad','NEW',x); END;]', -20065);
    debe_fallar('telefono con letras',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000018','otro5@test.com','90000000ab','Ciudad','NEW',x); END;]', -20065);
    debe_fallar('categoria no valida',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('Otro','900000019','otro6@test.com','9000000019','Ciudad','GOLD',x); END;]', -20067);
    debe_fallar('nombre vacio',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_cliente('   ','900000020','otro7@test.com','9000000020','Ciudad','NEW',x); END;]', -20060);

    BEGIN
        pkg_personas.sp_registrar_huesped('Huesped Dos', '900000021', 'huesped2@test.com', '9000000021', 'Valledupar', v_id);
        SELECT categoria INTO v_texto FROM clientes WHERE id = v_id;
        registrar(v_texto = 'NEW', 'el huesped que se registra solo entra siempre como NEW', '(categoria = ' || v_texto || ')');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'registrar huesped', '(llego ' || SQLERRM || ')');
    END;

    debe_funcionar('un ADMIN registra un trabajador',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_trabajador(900001,'Trab Nuevo','900000031','trabnuevo@test.com','9000000031','Valledupar','RECEPTIONIST','prueba900031','clave1',x); END;]');
    debe_fallar('un RECEPCIONISTA no puede registrar trabajadores',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_trabajador(900002,'Trab Dos','900000032','trabdos@test.com','9000000032','Valledupar','RECEPTIONIST','prueba900032','clave1',x); END;]', -20050);
    debe_fallar('un trabajador INACTIVO no puede registrar trabajadores',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_trabajador(900003,'Trab Tres','900000033','trabtres@test.com','9000000033','Valledupar','RECEPTIONIST','prueba900033','clave1',x); END;]', -20051);
    debe_fallar('usuario repetido (sin distinguir mayusculas)',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_trabajador(900001,'Trab Cuatro','900000034','trabcuatro@test.com','9000000034','Valledupar','ADMIN','PRUEBA900031','clave1',x); END;]', -20066);
    debe_fallar('rol no valido',
        q'[DECLARE x NUMBER; BEGIN pkg_personas.sp_registrar_trabajador(900001,'Trab Cinco','900000035','trabcinco@test.com','9000000035','Valledupar','BOSS','prueba900035','clave1',x); END;]', -20067);

    registrar(pkg_personas.fn_login('PRUEBA900001', 'x') = 900001, 'login correcto (usuario sin distinguir mayusculas)');
    debe_fallar('login con contrasena incorrecta',
        q'[DECLARE x NUMBER; BEGIN x := pkg_personas.fn_login('prueba900001','mal'); END;]', -20068);
    debe_fallar('login de un trabajador inactivo',
        q'[DECLARE x NUMBER; BEGIN x := pkg_personas.fn_login('prueba900003','z'); END;]', -20068);
    registrar(pkg_personas.fn_buscar_cliente_por_documento(' 900000001 ') = 900001, 'buscar cliente por documento');
    debe_fallar('buscar un documento que no existe',
        q'[DECLARE x NUMBER; BEGIN x := pkg_personas.fn_buscar_cliente_por_documento('no-existe'); END;]', -20069);

    -- ================================================================== PKG_HABITACIONES
    DECLARE
        v_numero_prueba        VARCHAR2(10);
        v_disponible_prueba    NUMBER;
        v_mantenimiento_prueba NUMBER;
        v_precio_prueba        NUMBER;
    BEGIN
        pkg_habitaciones.sp_registrar_habitacion(900001, ' T-903 ', 'DOUBLE', 3, 150000.456, v_hab);
        SELECT numero, disponible, en_mantenimiento, precio_noche
          INTO v_numero_prueba, v_disponible_prueba, v_mantenimiento_prueba, v_precio_prueba
          FROM habitaciones WHERE id = v_hab;
        registrar(
            v_numero_prueba = 'T-903'
            AND v_disponible_prueba = 1
            AND v_mantenimiento_prueba = 0
            AND v_precio_prueba = 150000.46,
            'registrar habitacion: queda disponible y el precio se redondea',
            '(quedo ' || v_numero_prueba || '|' || TO_CHAR(v_disponible_prueba) || '|'
                || TO_CHAR(v_mantenimiento_prueba) || '|' || TO_CHAR(v_precio_prueba) || ')'
        );
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'registrar habitacion valida', '(llego ' || SQLERRM || ')');
    END;
    debe_fallar('un RECEPCIONISTA no puede registrar habitaciones',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900002,'T-904','SINGLE',1,100000,x); END;]', -20050);
    debe_fallar('numero de habitacion repetido (sin distinguir mayusculas)',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900001,'t-901','SINGLE',1,100000,x); END;]', -20070);
    debe_fallar('tipo de habitacion no valido',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900001,'T-905','LUXURY',1,100000,x); END;]', -20071);
    debe_fallar('capacidad cero',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900001,'T-906','SINGLE',0,100000,x); END;]', -20071);
    debe_fallar('precio cero',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900001,'T-907','SINGLE',1,0,x); END;]', -20071);
    debe_fallar('precio que se redondea a cero',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900001,'T-908','SINGLE',1,0.004,x); END;]', -20071);
    debe_fallar('numero de habitacion vacio',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900001,'  ','SINGLE',1,100000,x); END;]', -20060);
    debe_fallar('numero de habitacion de mas de 6 caracteres',
        q'[DECLARE x NUMBER; BEGIN pkg_habitaciones.sp_registrar_habitacion(900001,'1234567','SINGLE',1,100000,x); END;]', -20060);

    -- ================================================================== PKG_RESERVAS
    registrar(pkg_reservas.fn_calcular_total(100000, 5, 'NEW') = 500000, 'total NEW, 5 noches = 500000');
    registrar(pkg_reservas.fn_calcular_total(100000, 7, 'NEW') = 630000, 'total NEW, 7 noches = 630000 (-10%)');
    registrar(pkg_reservas.fn_calcular_total(100000, 7, 'VIP') = 535500, 'total VIP, 7 noches = 535500 (-10% y -15%)');
    registrar(pkg_reservas.fn_calcular_total(100000, 2, 'FREQUENT') = 190000, 'total FREQUENT, 2 noches = 190000 (-5%)');

    BEGIN
        pkg_reservas.sp_crear_reserva(900001, 900001, 900001, v_hoy, v_d5, 1, v_resA);
        SELECT TO_CHAR(total) INTO v_texto FROM reservas WHERE id = v_resA;
        registrar(v_texto = '500000', 'crear reserva A: el total lo calcula Oracle', '(total = ' || v_texto || ')');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'crear reserva A', '(llego ' || SQLERRM || ')');
            RAISE;
    END;
    BEGIN
        pkg_reservas.sp_crear_reserva(900001, NULL, 900001, v_d10, v_d12, 1, v_resB);
        registrar(TRUE, 'crear reserva B sin trabajador (autoservicio del huesped)');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'crear reserva B sin trabajador', '(llego ' || SQLERRM || ')');
            RAISE;
    END;
    BEGIN
        pkg_reservas.sp_crear_reserva(900002, 900001, 900002, v_d20, v_d22, 2, v_resC);
        SELECT TO_CHAR(total) INTO v_texto FROM reservas WHERE id = v_resC;
        registrar(v_texto = '340000', 'cliente VIP, 2 noches x 200000 = 340000 (-15%)', '(total = ' || v_texto || ')');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'crear reserva C (VIP)', '(llego ' || SQLERRM || ')');
            RAISE;
    END;
    BEGIN
        pkg_reservas.sp_crear_reserva(900002, NULL, 900002, v_d30, v_d31, 1, v_resD);
        registrar(TRUE, 'crear reserva D');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'crear reserva D', '(llego ' || SQLERRM || ')');
            RAISE;
    END;

    -- Las fechas se guardan siempre como AAAA-MM-DD, aunque lleguen como '2031-3-1'.
    BEGIN
        pkg_reservas.sp_crear_reserva(900001, 900001, 900002, '2031-3-1', '2031-3-3', 1, v_num);
        SELECT entrada || '>' || salida INTO v_texto FROM reservas WHERE id = v_num;
        registrar(v_texto = '2031-03-01>2031-03-03', 'las fechas se guardan normalizadas AAAA-MM-DD', '(quedo ' || v_texto || ')');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'normalizar fechas al crear la reserva', '(llego ' || SQLERRM || ')');
    END;

    debe_fallar('reserva que se cruza (hoy+3 -> hoy+8)',
        'DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900001,900001,''' || v_d3 || ''',''' || v_d8 || ''',1,x); END;', -20003);
    debe_fallar('mas huespedes que la capacidad',
        q'[DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900001,900001,'2032-03-01','2032-03-03',3,x); END;]', -20032);
    debe_fallar('cliente que no existe',
        q'[DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(999999999,900001,900001,'2032-03-01','2032-03-03',1,x); END;]', -20042);
    debe_fallar('trabajador inactivo',
        q'[DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900003,900001,'2032-03-01','2032-03-03',1,x); END;]', -20037);
    debe_fallar('entrada anterior a hoy',
        'DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900001,900001,''' || v_ayer2 || ''',''' || v_d3 || ''',1,x); END;', -20040);
    debe_fallar('salida igual a la entrada',
        q'[DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900001,900001,'2032-03-01','2032-03-01',1,x); END;]', -20001);
    debe_fallar('fecha con formato invalido',
        q'[DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900001,900001,'manana','2032-03-01',1,x); END;]', -20004);
    debe_fallar('fechas vacias',
        q'[DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900001,900001,NULL,NULL,1,x); END;]', -20060);

    -- Habitacion de la prueba de mantenimiento (900001 tiene reservas A y B activas).
    debe_fallar('mantenimiento con reservas activas',
        q'[BEGIN pkg_habitaciones.sp_iniciar_mantenimiento(900001, 900001); END;]', -20074);
    debe_fallar('un RECEPCIONISTA no puede iniciar mantenimiento',
        q'[BEGIN pkg_habitaciones.sp_iniciar_mantenimiento(900002, 900001); END;]', -20050);
    debe_fallar('finalizar mantenimiento de una habitacion que no lo esta',
        q'[BEGIN pkg_habitaciones.sp_finalizar_mantenimiento(900001, 900001); END;]', -20073);
    debe_fallar('mantenimiento de una habitacion que no existe',
        q'[BEGIN pkg_habitaciones.sp_iniciar_mantenimiento(900001, 999999999); END;]', -20030);

    -- Estados.
    debe_fallar('salida sin haber registrado la llegada',
        'BEGIN pkg_reservas.sp_checkout(' || v_resA || '); END;', -20033);
    debe_fallar('llegada antes de la fecha de entrada',
        'BEGIN pkg_reservas.sp_checkin(' || v_resB || '); END;', -20041);
    debe_funcionar('llegada (check-in)', 'BEGIN pkg_reservas.sp_checkin(' || v_resA || '); END;');
    debe_funcionar('salida (check-out)', 'BEGIN pkg_reservas.sp_checkout(' || v_resA || '); END;');
    debe_fallar('cancelar una reserva que ya tuvo la llegada registrada',
        'BEGIN pkg_reservas.sp_cancelar_reserva(' || v_resA || ', 900001); END;', -20033);

    -- Comprobante y pagos de la reserva A (total 500000).
    BEGIN
        pkg_reservas.sp_generar_comprobante(v_resA, 'CASH', v_compA);
        pkg_reservas.sp_registrar_pago(v_compA, 200000);
        SELECT estado INTO v_texto FROM comprobantes WHERE id = v_compA;
        registrar(v_texto = 'PARTIALLY_PAID', 'abono parcial deja el comprobante PARTIALLY_PAID', '(estado = ' || v_texto || ')');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'generar comprobante y abono parcial', '(llego ' || SQLERRM || ')');
            RAISE;
    END;
    debe_fallar('segundo comprobante para la misma reserva',
        'DECLARE x NUMBER; BEGIN pkg_reservas.sp_generar_comprobante(' || v_resA || ', ''CARD'', x); END;', -20039);
    debe_fallar('metodo de pago no valido',
        'DECLARE x NUMBER; BEGIN pkg_reservas.sp_generar_comprobante(' || v_resD || ', ''CHEQUE'', x); END;', -20060);
    debe_fallar('abono que supera el saldo',
        'BEGIN pkg_reservas.sp_registrar_pago(' || v_compA || ', 400000); END;', -20035);
    debe_fallar('abono con mas de 2 decimales',
        'BEGIN pkg_reservas.sp_registrar_pago(' || v_compA || ', 10.123); END;', -20035);
    debe_fallar('abono de cero',
        'BEGIN pkg_reservas.sp_registrar_pago(' || v_compA || ', 0); END;', -20035);
    debe_funcionar('abono del saldo restante',
        'BEGIN pkg_reservas.sp_registrar_pago(' || v_compA || ', 300000); END;');
    SELECT estado INTO v_texto FROM comprobantes WHERE id = v_compA;
    registrar(v_texto = 'PAID', 'comprobante pagado completo queda PAID', '(estado = ' || v_texto || ')');
    debe_fallar('abonar un comprobante que ya esta PAID',
        'BEGIN pkg_reservas.sp_registrar_pago(' || v_compA || ', 1000); END;', -20034);
    registrar(pkg_reservas.fn_tiene_pagos(v_resA) = 1, 'fn_tiene_pagos = 1 cuando hay abonos');
    registrar(pkg_reservas.fn_tiene_pagos(v_resD) = 0, 'fn_tiene_pagos = 0 cuando no hay abonos');

    -- Cancelaciones con pagos (reserva B).
    pkg_reservas.sp_generar_comprobante(v_resB, 'TRANSFER', v_compB);
    pkg_reservas.sp_registrar_pago(v_compB, 50000);
    debe_fallar('un RECEPCIONISTA no puede cancelar una reserva con pagos',
        'BEGIN pkg_reservas.sp_cancelar_reserva(' || v_resB || ', 900002); END;', -20050);
    debe_funcionar('un ADMIN cancela una reserva con abono parcial',
        'BEGIN pkg_reservas.sp_cancelar_reserva(' || v_resB || ', 900001); END;');
    SELECT estado || '|' || pagado INTO v_texto FROM comprobantes WHERE id = v_compB;
    registrar(v_texto = 'VOIDED|50000', 'el comprobante queda VOIDED y conserva el abono', '(quedo ' || v_texto || ')');
    SELECT estado INTO v_texto FROM reservas WHERE id = v_resB;
    registrar(v_texto = 'CANCELLED', 'la reserva B queda CANCELLED', '(estado = ' || v_texto || ')');

    -- Cancelaciones del huesped (reservas C y D son del cliente 900002).
    pkg_reservas.sp_generar_comprobante(v_resC, 'CARD', v_compC);
    pkg_reservas.sp_registrar_pago(v_compC, 30000);
    debe_fallar('un huesped no puede cancelar la reserva de otro cliente',
        'BEGIN pkg_reservas.sp_cancelar_reserva_huesped(900001, ' || v_resC || '); END;', -20052);
    debe_fallar('un huesped no puede cancelar una reserva que ya tiene pagos',
        'BEGIN pkg_reservas.sp_cancelar_reserva_huesped(900002, ' || v_resC || '); END;', -20053);
    debe_funcionar('un huesped cancela su reserva sin pagos',
        'BEGIN pkg_reservas.sp_cancelar_reserva_huesped(900002, ' || v_resD || '); END;');
    debe_fallar('cancelar una reserva ya cancelada',
        'BEGIN pkg_reservas.sp_cancelar_reserva(' || v_resD || ', 900001); END;', -20033);

    -- Habitaciones libres: la 900001 esta libre entre hoy+14 y hoy+15; con mantenimiento deja de estarlo.
    DECLARE
        c      SYS_REFCURSOR;
        r_id   NUMBER;
        r_num  VARCHAR2(10);
        r_tipo VARCHAR2(10);
        r_cap  NUMBER;
        r_prec NUMBER;
        r_disp NUMBER;
        r_mant NUMBER;
        v_vio  NUMBER := 0;
    BEGIN
        c := pkg_reservas.fn_habitaciones_libres(TO_CHAR(SYSDATE + 14, 'YYYY-MM-DD'), TO_CHAR(SYSDATE + 15, 'YYYY-MM-DD'), 1);
        LOOP
            FETCH c INTO r_id, r_num, r_tipo, r_cap, r_prec, r_disp, r_mant;
            EXIT WHEN c%NOTFOUND;
            IF r_id = 900001 THEN v_vio := 1; END IF;
        END LOOP;
        CLOSE c;
        registrar(v_vio = 1, 'habitaciones libres: aparece la habitacion sin cruces');
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'habitaciones libres', '(llego ' || SQLERRM || ')');
    END;
    debe_funcionar('iniciar mantenimiento sin reservas activas (reserva B cancelada, A finalizada)',
        'BEGIN pkg_habitaciones.sp_iniciar_mantenimiento(900001, 900001); END;');
    debe_fallar('iniciar mantenimiento dos veces',
        'BEGIN pkg_habitaciones.sp_iniciar_mantenimiento(900001, 900001); END;', -20072);
    debe_fallar('reservar una habitacion en mantenimiento',
        q'[DECLARE x NUMBER; BEGIN pkg_reservas.sp_crear_reserva(900001,900001,900001,'2032-03-01','2032-03-03',1,x); END;]', -20031);
    debe_funcionar('finalizar mantenimiento',
        'BEGIN pkg_habitaciones.sp_finalizar_mantenimiento(900001, 900001); END;');

    -- ================================================================== PKG_REPORTES
    DECLARE
        c        SYS_REFCURSOR;
        e_total  NUMBER; e_ocu NUMBER; e_res NUMBER; e_lib NUMBER; e_man NUMBER; e_nod NUMBER;
        e_ing    NUMBER; e_sal NUMBER;
        v_real   NUMBER;
        v_filas  NUMBER;
        s_estado VARCHAR2(20); s_cant NUMBER; s_valor NUMBER;
        u_num    VARCHAR2(10); u_tipo VARCHAR2(10); u_veces NUMBER; u_noches NUMBER; u_valor NUMBER;
        m_mes    VARCHAR2(7); m_comp NUMBER; m_cob NUMBER; m_reem NUMBER; m_neto NUMBER;
        b_id     NUMBER; b_cli VARCHAR2(100); b_fecha VARCHAR2(10); b_monto NUMBER; b_pag NUMBER; b_saldo NUMBER; b_est VARCHAR2(20);
        k_nom    VARCHAR2(100); k_cat VARCHAR2(10); k_res NUMBER; k_noc NUMBER; k_val NUMBER;
        t_id     NUMBER; t_num VARCHAR2(10); t_tipo VARCHAR2(10); t_veces NUMBER; t_noches NUMBER;
        v_anterior NUMBER;
        v_orden  NUMBER;
        v_encontro NUMBER;
    BEGIN
        -- Estadisticas generales.
        c := pkg_reportes.fn_estadisticas_generales(900001);
        FETCH c INTO e_total, e_ocu, e_res, e_lib, e_man, e_nod, e_ing, e_sal;
        CLOSE c;
        SELECT COUNT(*) INTO v_real FROM habitaciones;
        registrar(e_total = v_real, 'estadisticas: total de habitaciones', '(' || e_total || ' vs ' || v_real || ')');
        registrar(e_ocu + e_res + e_lib + e_man + e_nod = e_total,
                  'estadisticas: cada habitacion cae en una sola clase',
                  '(suma ' || (e_ocu + e_res + e_lib + e_man + e_nod) || ' vs ' || e_total || ')');
        SELECT NVL(SUM(pagado - reembolso), 0) INTO v_real FROM comprobantes;
        registrar(e_ing = v_real, 'estadisticas: ingresos = abonado - reembolsado', '(' || e_ing || ' vs ' || v_real || ')');
        SELECT NVL(SUM(monto - pagado), 0) INTO v_real FROM comprobantes WHERE estado IN ('PENDING', 'PARTIALLY_PAID');
        registrar(e_sal = v_real, 'estadisticas: saldo pendiente solo de comprobantes con saldo', '(' || e_sal || ' vs ' || v_real || ')');

        debe_fallar('estadisticas: un RECEPCIONISTA no tiene acceso',
            q'[DECLARE c SYS_REFCURSOR; BEGIN c := pkg_reportes.fn_estadisticas_generales(900002); END;]', -20050);
        debe_fallar('estadisticas: un trabajador INACTIVO no tiene acceso',
            q'[DECLARE c SYS_REFCURSOR; BEGIN c := pkg_reportes.fn_estadisticas_generales(900003); END;]', -20051);

        -- Reservas por estado: siempre 4 filas y las cantidades coinciden con las tablas.
        c := pkg_reportes.fn_reservas_por_estado(900001, NULL, NULL);
        v_filas := 0;
        v_orden := 0;
        LOOP
            FETCH c INTO s_estado, s_cant, s_valor;
            EXIT WHEN c%NOTFOUND;
            v_filas := v_filas + 1;
            SELECT COUNT(*) INTO v_real FROM reservas WHERE estado = s_estado;
            IF s_cant <> v_real THEN v_orden := v_orden + 1; END IF;
        END LOOP;
        CLOSE c;
        registrar(v_filas = 4, 'reservas por estado: devuelve los 4 estados', '(filas = ' || v_filas || ')');
        registrar(v_orden = 0, 'reservas por estado: las cantidades coinciden con la tabla', '(' || v_orden || ' estados difieren)');

        debe_fallar('reportes: rango con la fecha final anterior a la inicial',
            q'[DECLARE c SYS_REFCURSOR; BEGIN c := pkg_reportes.fn_reservas_por_estado(900001,'2031-02-01','2031-01-01'); END;]', -20080);
        debe_fallar('reportes: fecha con formato invalido',
            q'[DECLARE c SYS_REFCURSOR; BEGIN c := pkg_reportes.fn_ingresos_por_mes(900001,'abc',NULL); END;]', -20004);

        -- Reservas por estado con rango: solo la reserva '2031-03-01' del paso de normalizacion cae ahi.
        c := pkg_reportes.fn_reservas_por_estado(900001, '2031-03-01', '2031-03-31');
        v_real := 0;
        LOOP
            FETCH c INTO s_estado, s_cant, s_valor;
            EXIT WHEN c%NOTFOUND;
            IF s_estado = 'ACTIVE' THEN v_real := s_cant; END IF;
        END LOOP;
        CLOSE c;
        SELECT COUNT(*) INTO v_filas FROM reservas WHERE estado = 'ACTIVE' AND entrada BETWEEN '2031-03-01' AND '2031-03-31';
        registrar(v_real = v_filas AND v_real >= 1, 'reservas por estado: el rango filtra por fecha de entrada', '(' || v_real || ' vs ' || v_filas || ')');

        -- Uso de habitaciones: T-901 tiene 1 reserva no cancelada (A: 5 noches, 500000); B esta cancelada.
        c := pkg_reportes.fn_uso_habitaciones(900001, NULL, NULL);
        v_encontro := 0;
        v_anterior := NULL;
        v_orden := 0;
        LOOP
            FETCH c INTO u_num, u_tipo, u_veces, u_noches, u_valor;
            EXIT WHEN c%NOTFOUND;
            IF u_num = 'T-901' THEN
                v_encontro := 1;
                registrar(u_veces = 1 AND u_noches = 5 AND u_valor = 500000,
                          'uso de habitaciones: T-901 = 1 vez, 5 noches, 500000 (sin contar la cancelada)',
                          '(veces=' || u_veces || ' noches=' || u_noches || ' valor=' || u_valor || ')');
            END IF;
            IF v_anterior IS NOT NULL AND u_veces > v_anterior THEN v_orden := v_orden + 1; END IF;
            v_anterior := u_veces;
        END LOOP;
        CLOSE c;
        registrar(v_encontro = 1, 'uso de habitaciones: aparece T-901');
        registrar(v_orden = 0, 'uso de habitaciones: ordenado de la mas a la menos usada');

        -- Ingresos por mes: el mes actual incluye lo abonado en A (500000) y en B (50000, anulado) y C (30000).
        c := pkg_reportes.fn_ingresos_por_mes(900001, NULL, NULL);
        v_real := NULL;
        LOOP
            FETCH c INTO m_mes, m_comp, m_cob, m_reem, m_neto;
            EXIT WHEN c%NOTFOUND;
            IF m_mes = TO_CHAR(SYSDATE, 'YYYY-MM') THEN v_real := m_cob; END IF;
        END LOOP;
        CLOSE c;
        registrar(v_real IS NOT NULL AND v_real >= 580000, 'ingresos por mes: el mes actual suma lo abonado', '(cobrado = ' || v_real || ')');

        -- Saldos pendientes: C (PARTIALLY_PAID, 340000 - 30000 = 310000) si; A (PAID) y B (VOIDED) no.
        c := pkg_reportes.fn_saldos_pendientes(900001, NULL, NULL);
        v_encontro := 0;
        v_orden := 0;
        LOOP
            FETCH c INTO b_id, b_cli, b_fecha, b_monto, b_pag, b_saldo, b_est;
            EXIT WHEN c%NOTFOUND;
            IF b_id = v_compC THEN
                v_encontro := 1;
                registrar(b_saldo = 310000 AND b_est = 'PARTIALLY_PAID',
                          'saldos pendientes: el comprobante de C debe 310000',
                          '(saldo=' || b_saldo || ' estado=' || b_est || ')');
            END IF;
            IF b_id IN (v_compA, v_compB) THEN v_orden := v_orden + 1; END IF;
        END LOOP;
        CLOSE c;
        registrar(v_encontro = 1, 'saldos pendientes: aparece el comprobante parcialmente pagado');
        registrar(v_orden = 0, 'saldos pendientes: no aparecen los comprobantes PAID ni VOIDED');

        -- Mejores clientes: maximo 10 filas, de mayor a menor valor.
        c := pkg_reportes.fn_mejores_clientes(900001, NULL, NULL);
        v_filas := 0;
        v_anterior := NULL;
        v_orden := 0;
        LOOP
            FETCH c INTO k_nom, k_cat, k_res, k_noc, k_val;
            EXIT WHEN c%NOTFOUND;
            v_filas := v_filas + 1;
            IF v_anterior IS NOT NULL AND k_val > v_anterior THEN v_orden := v_orden + 1; END IF;
            v_anterior := k_val;
        END LOOP;
        CLOSE c;
        registrar(v_filas BETWEEN 1 AND 10, 'mejores clientes: entre 1 y 10 filas', '(filas = ' || v_filas || ')');
        registrar(v_orden = 0, 'mejores clientes: ordenado por valor reservado');

        -- Ranking de habitaciones (sin control de rol).
        c := pkg_reportes.fn_top_habitaciones(NULL, 1);
        v_filas := 0;
        LOOP
            FETCH c INTO t_id, t_num, t_tipo, t_veces, t_noches;
            EXIT WHEN c%NOTFOUND;
            v_filas := v_filas + 1;
        END LOOP;
        CLOSE c;
        registrar(v_filas = 1, 'ranking: el limite 1 devuelve una sola habitacion', '(filas = ' || v_filas || ')');

        c := pkg_reportes.fn_top_habitaciones(NULL, NULL);
        v_filas := 0;
        LOOP
            FETCH c INTO t_id, t_num, t_tipo, t_veces, t_noches;
            EXIT WHEN c%NOTFOUND;
            v_filas := v_filas + 1;
        END LOOP;
        CLOSE c;
        SELECT COUNT(*) INTO v_real FROM habitaciones;
        registrar(v_filas = v_real, 'ranking: sin limite devuelve todas las habitaciones', '(' || v_filas || ' vs ' || v_real || ')');

        -- Con un periodo que empieza despues de todas las reservas de prueba, T-901 queda en 0.
        c := pkg_reportes.fn_top_habitaciones(TO_CHAR(SYSDATE + 100, 'YYYY-MM-DD'), NULL);
        v_real := NULL;
        LOOP
            FETCH c INTO t_id, t_num, t_tipo, t_veces, t_noches;
            EXIT WHEN c%NOTFOUND;
            IF t_num = 'T-901' THEN v_real := t_veces; END IF;
        END LOOP;
        CLOSE c;
        registrar(v_real = 0, 'ranking: el periodo filtra por fecha de entrada', '(veces = ' || v_real || ')');

        debe_fallar('ranking: limite cero no es valido',
            q'[DECLARE c SYS_REFCURSOR; BEGIN c := pkg_reportes.fn_top_habitaciones(NULL, 0); END;]', -20060);
    EXCEPTION
        WHEN OTHERS THEN
            registrar(FALSE, 'bloque de reportes', '(llego ' || SQLERRM || ')');
    END;

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE(v_pasan || ' pasan, ' || v_fallan || ' fallan');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/