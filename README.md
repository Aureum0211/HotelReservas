# HotelReservas

Sistema de gestión hotelera desarrollado con **Java 17, JavaFX, JDBC y Oracle Database**. Permite administrar huéspedes, trabajadores, habitaciones, reservas, comprobantes, pagos, mantenimiento y reportes.

## Funcionalidades

- Inicio de sesión de trabajadores con roles `ADMIN` y `RECEPTIONIST`.
- Registro e identificación de huéspedes.
- Gestión independiente de clientes y trabajadores.
- Registro y consulta de habitaciones.
- Búsqueda de habitaciones disponibles por fechas y capacidad.
- Creación y cancelación de reservas.
- Check-in y check-out.
- Gestión de mantenimiento de habitaciones.
- Generación de comprobantes y registro de pagos parciales o completos.
- Reportes de reservas, ingresos, uso de habitaciones, mejores clientes y saldos pendientes.
- Ranking de habitaciones populares.
- Auditoría de cambios de estado de reservas en Oracle.

## Tecnologías y requisitos

- JDK 17 o superior.
- Apache Maven 3.8 o superior.
- Oracle Database con soporte PL/SQL y un servicio accesible desde el equipo.
- SQL Developer (recomendado para instalar y administrar los scripts; no es necesario en los equipos que solo ejecutan la aplicación).
- NetBeans es opcional; también se puede ejecutar desde una terminal.

## Arquitectura

La aplicación separa la interfaz, la comunicación y la lógica de negocio:

- **JavaFX:** ventanas, formularios, tablas, navegación y mensajes.
- **`HotelService`:** fachada entre los controladores y la persistencia; adapta los datos de Java a los parámetros que espera Oracle.
- **`OracleHotelProcedures`:** centraliza las llamadas JDBC a procedimientos y funciones PL/SQL.
- **`JdbcRepository` y `SqlMappers`:** consultan registros y convierten filas de Oracle en modelos Java.
- **Oracle Database:** almacena la información y ejecuta las reglas de negocio, validaciones, cálculos, operaciones de escritura, triggers y auditoría.

Los paquetes principales son `PKG_RESERVAS`, `PKG_PERSONAS`, `PKG_HABITACIONES` y `PKG_REPORTES`.

## Instalación de Oracle

Los scripts SQL/PLSQL están en [`oracle/`](oracle/). Antes de ejecutar la aplicación, prepara un esquema Oracle y ejecuta los scripts siguiendo la guía:

**[Guía de instalación, configuración y conexión multi-PC](oracle/README.md)**

La guía explica cómo crear un usuario/esquema, ejecutar los diez scripts en orden, comprobar los paquetes y conectar varios equipos a una única instancia Oracle.

> Los scripts preparan los objetos de HotelReservas dentro de una instancia Oracle existente. No instalan Oracle Database ni crean el servidor de base de datos.

## Configurar la conexión

En la raíz del proyecto (junto a `pom.xml`), crea `db.properties` a partir de la plantilla:

**Windows PowerShell:**

```powershell
Copy-Item db.properties.example db.properties
```

**Windows CMD:**

```cmd
copy db.properties.example db.properties
```

Edita `db.properties` y completa los tres valores de tu conexión:

```properties
db.url=jdbc:oracle:thin:@//HOST:1521/SERVICE_NAME
db.user=HOTEL
db.password=TU_CLAVE_DEL_ESQUEMA
```

Usa el host, puerto y service name reales de tu instalación. `localhost` solo sirve si Oracle está en el mismo equipo que ejecuta HotelReservas. Para una computadora cliente, utiliza la IP privada o el nombre de red del equipo que aloja Oracle.

**No publiques `db.properties` ni incluyas contraseñas reales en GitHub.** El repositorio contiene únicamente `db.properties.example`, que es una plantilla sin credenciales válidas. El archivo local `db.properties` está excluido mediante `.gitignore`.

## Ejecutar la aplicación

Desde una terminal ubicada en la carpeta que contiene `pom.xml` y `db.properties`:

```bash
mvn clean javafx:run
```

En NetBeans, abre el proyecto como proyecto Maven y ejecuta el objetivo `javafx:run`.

La aplicación utiliza el lanzador JavaFX `ui.AppFxmlLauncher`. No es necesario iniciar un menú de consola.

## Instalación en varias computadoras

HotelReservas puede ejecutarse en varios equipos conectados a una misma instancia Oracle. Una computadora aloja Oracle Database; las demás ejecutan la aplicación y apuntan al mismo host, puerto, servicio y esquema mediante su propio `db.properties`.

Los equipos cliente no necesitan instalar otra base de datos ni ejecutar los scripts SQL. Solo necesitan Java, Maven (si ejecutan desde el código fuente), el proyecto y una configuración de conexión válida.

Consulta la sección **Conectar varias computadoras a la misma base de datos** de [`oracle/README.md`](oracle/README.md) para configurar listener, firewall y red local. No expongas el puerto Oracle directamente a Internet.

## Estructura principal

```text
HotelReservas/
├── src/
│   └── main/
│       ├── java/             Código Java
│       └── resources/        FXML, CSS e imágenes
├── oracle/                   Scripts SQL/PLSQL y guía de instalación
├── docs/                     Análisis, diagramas y notas de pruebas
├── db.properties.example     Plantilla de conexión
├── pom.xml                   Dependencias y ejecución Maven
├── .gitignore
└── README.md
```

## Cuentas de demostración

En una instalación nueva, `04_base_comun.sql` agrega trabajadores de demostración solo si la tabla `TRABAJADORES` está vacía:

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `recepcion1` | `recep123` | `RECEPTIONIST` |
| `recepcion2` | `recep456` | `RECEPTIONIST` |

Son credenciales de demostración académica para iniciar sesión en HotelReservas. No son las credenciales de conexión de Oracle configuradas en `db.properties`.

## Documentación adicional

- [`docs/analisis.md`](docs/analisis.md): propósito, modelo de dominio y responsabilidades de Java y Oracle.
- [`docs/layers-diagram.md`](docs/layers-diagram.md): arquitectura por capas.
- [`docs/class-diagram.md`](docs/class-diagram.md): clases y relaciones.
- [`docs/hierarchy-diagram.md`](docs/hierarchy-diagram.md): jerarquía de modelos y excepciones.
- [`docs/REVISION_Y_PRUEBAS.md`](docs/REVISION_Y_PRUEBAS.md): comprobaciones y lista de verificación.
