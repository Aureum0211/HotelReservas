# Diseño del sistema HotelReservas

## Propósito

HotelReservas permite administrar huéspedes, trabajadores, habitaciones, reservas, comprobantes y pagos. La interfaz está desarrollada con JavaFX y los datos se almacenan en Oracle Database.

## Modelo de dominio

- `Person` es la clase abstracta que contiene los datos compartidos de una persona.
- `Client` hereda de `Person` y añade la categoría `NEW`, `FREQUENT` o `VIP`.
- `Employee` hereda de `Person` y añade rol, usuario y contraseña. Los roles del sistema son `ADMIN` y `RECEPTIONIST`.
- `Room` representa número, tipo, capacidad, tarifa, disponibilidad y estado de mantenimiento.
- `Reservation` relaciona cliente, trabajador opcional, habitación, fechas, cantidad de huéspedes, tarifa, total y estado.
- `Invoice` relaciona un comprobante con una reserva e incluye monto, método de pago, estado, pagos y reembolsos.
- `ReportRows` contiene records de solo lectura que representan los resultados de los cursores de reportes devueltos por Oracle.

## Responsabilidad de Java y Oracle

La arquitectura separa la interfaz, la comunicación y las reglas de negocio:

1. **JavaFX:** recibe datos, muestra tablas, formularios, mensajes y reportes.
2. **`HotelService`:** funciona como fachada entre los controladores y la persistencia. Convierte fechas, enumeraciones y objetos a los parámetros que espera Oracle.
3. **`OracleHotelProcedures`:** concentra las llamadas JDBC a procedimientos y funciones PL/SQL. Traduce los errores Oracle a las excepciones del proyecto.
4. **`JdbcRepository` y `SqlMappers`:** realizan consultas de lectura y convierten filas Oracle en modelos Java.
5. **Oracle:** aplica las reglas de negocio, realiza escrituras, calcula importes y reportes, mantiene restricciones y registra auditoría.

## Paquetes PL/SQL

- `PKG_RESERVAS`: crea y cancela reservas, calcula precios, consulta disponibilidad, registra check-in/check-out, genera comprobantes y registra pagos.
- `PKG_PERSONAS`: registra clientes, huéspedes y trabajadores; valida unicidad y permite iniciar sesión.
- `PKG_HABITACIONES`: registra habitaciones y gestiona su mantenimiento.
- `PKG_REPORTES`: devuelve estadísticas generales, reservas por estado, ingresos por mes, uso de habitaciones, mejores clientes, saldos pendientes y ranking de habitaciones.

Además, `FN_NOCHES` calcula la duración de una reserva y `FN_HAY_SOLAPE` detecta cruces de fechas. Los triggers de `RESERVAS` validan las transiciones de estado, evitan solapamientos y registran cambios en `AUDITORIA_RESERVAS`.

## Repositorios

Los repositorios Java son de solo lectura (`findById` y `findAll`). Las operaciones de negocio que insertan o actualizan datos se realizan mediante los paquetes PL/SQL, evitando duplicar reglas entre Java y Oracle.

## Reportes

Los reportes y el ranking se calculan en Oracle. `Reportes` y `TopHabitacionesController` convierten los resultados recibidos en textos y elementos visuales; no vuelven a contar reservas ni a sumar importes desde las entidades.

## Manejo de errores

Los errores `ORA-20xxx` definidos en los paquetes se convierten en `BusinessRuleException`, `InvalidDataException` o `ResourceNotFoundException`. La interfaz muestra el mensaje resultante mediante las utilidades de `Ui`.

## Alcance académico

El proyecto conserva el esquema de contraseñas en texto plano y las fechas almacenadas como texto `AAAA-MM-DD`, según las decisiones del trabajo académico. La instalación de tablas, paquetes, funciones y triggers se realiza mediante los scripts de `oracle/`, no desde Java.
