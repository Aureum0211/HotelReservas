# HotelReservas

Sistema de gestión hotelera desarrollado con **Java 17+, JavaFX, JDBC y Oracle Database**.
La interfaz permite gestionar huéspedes, trabajadores, habitaciones, reservas, comprobantes,
pagos, mantenimiento y reportes.

## Arquitectura

- **JavaFX:** pantallas, formularios, tablas, navegación y mensajes.
- **HotelService:** fachada entre los controladores y la persistencia.
- **OracleHotelProcedures:** centraliza las llamadas JDBC a los paquetes PL/SQL.
- **JdbcRepository / SqlMappers:** consultas de solo lectura y conversión de filas Oracle a modelos Java.
- **Oracle:** reglas de negocio, validaciones, cálculos, operaciones de escritura, triggers, auditoría y almacenamiento.

Paquetes principales: `PKG_RESERVAS`, `PKG_PERSONAS`, `PKG_HABITACIONES` y `PKG_REPORTES`.

## Requisitos

- JDK 17 o superior.
- Apache Maven 3.8+.
- Oracle Database accesible desde el equipo.
- NetBeans (opcional).

## Preparar la base de datos

1. Abre SQL Developer y conéctate al esquema que usará la aplicación.
2. Sigue el orden y las precauciones de [`oracle/LEEME.md`](oracle/LEEME.md).
3. Ejecuta las pruebas del script `16_pruebas_paquetes.sql` y confirma que indique `0 fallan`.

La aplicación ya no crea tablas ni inserta trabajadores desde Java. El esquema y los usuarios iniciales
se preparan con scripts SQL. No ejecutes scripts históricos de recreación sobre una base que contenga datos.

## Configurar conexión

En la raíz del proyecto, crea o edita `db.properties` con los parámetros de tu Oracle:

```properties
db.url=jdbc:oracle:thin:@//HOST:PUERTO/SERVICIO
db.user=USUARIO_ESQUEMA
db.password=CONTRASENA
```

Sustituye los valores por los de tu conexión. No publiques credenciales reales.

## Ejecutar

Desde la raíz del proyecto:

```bash
mvn clean javafx:run
```

También puedes abrir el proyecto Maven en NetBeans y ejecutar el objetivo `javafx:run`.

La aplicación comprueba al iniciar que los cuatro paquetes Oracle estén instalados y válidos. Si falta alguno,
mostrará un mensaje indicando que debes ejecutar los scripts de `oracle/`.

## Funcionalidades

- Inicio de sesión de trabajadores con roles ADMIN y RECEPTIONIST.
- Registro e identificación de huéspedes.
- Registro y consulta de clientes, trabajadores y habitaciones.
- Búsqueda de habitaciones disponibles por fechas y capacidad.
- Creación y cancelación de reservas.
- Check-in y check-out.
- Gestión de mantenimiento.
- Comprobantes y pagos parciales o completos.
- Reportes de reservas, ingresos, uso de habitaciones, mejores clientes y saldos pendientes.
- Ranking de habitaciones populares.
- Auditoría de cambios de estado de reservas en Oracle.

## Estructura

```text
src/main/java/
├── Main.java
├── exceptions/       Excepciones de la aplicación
├── model/            Modelos de datos y resultados de reportes
├── persistence/      Conexión, consultas, mappers y llamadas PL/SQL
├── service/          Fachada HotelService
├── ui/               JavaFX, controladores y FXML
└── validation/       Validaciones básicas de entrada/modelo

oracle/               Scripts SQL y PL/SQL de instalación y pruebas
```

## Notas

- Las operaciones de negocio se ejecutan en Oracle, no en los controladores Java.
- Los repositorios Java consultan y reconstruyen modelos; las escrituras de negocio se delegan a PL/SQL.
- Se conserva el esquema académico de contraseñas en texto plano indicado para esta entrega.
- Las fechas de las tablas se mantienen como texto `AAAA-MM-DD`, según el esquema existente.
