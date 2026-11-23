# Hotel Reservas
## Documentación del proyecto de software

**Institución:** Universidad Popular del Cesar  
**Programa:** Ingeniería de Sistemas  
**Asignatura:** Programación III  
**Autor:** Edward Carrillo  
**Docente:** Alfredo Bautista  
**Lugar y año:** Valledupar, Cesar — 2026

---

## Resumen

Hotel Reservas es una aplicación de escritorio desarrollada en Java 17 con JavaFX, JDBC y Oracle Database. Permite administrar huéspedes, trabajadores, habitaciones, reservas, comprobantes y pagos. También incluye consulta de disponibilidad, control de mantenimiento, reportes y un ranking de habitaciones populares.

La aplicación utiliza JavaFX para la interfaz y JDBC para comunicarse con Oracle. La lógica de negocio se concentra principalmente en paquetes PL/SQL, donde se realizan las validaciones, los cálculos y las operaciones de escritura. Java organiza las solicitudes, recupera los resultados y los presenta al usuario. La base de datos conserva la información entre ejecuciones.

El proyecto se desarrolló con fines académicos para aplicar programación orientada a objetos, separación de responsabilidades, manejo de excepciones, acceso a datos y diseño por capas. La solución puede ejecutarse desde distintos equipos cuando todos tienen acceso de red a una misma instancia Oracle y cuentan con su propia configuración de conexión.

**Palabras clave:** gestión hotelera, Java, JavaFX, JDBC, Oracle Database, PL/SQL, reservas.

## 1. Introducción

La gestión de un hotel requiere mantener relacionados los datos de los huéspedes, trabajadores, habitaciones, reservas, fechas de entrada y salida, comprobantes y pagos. También es necesario conocer la disponibilidad de cada habitación, evitar reservas incompatibles y conservar un registro de las operaciones realizadas.

Hotel Reservas reúne estas operaciones en una aplicación de escritorio. La interfaz permite interactuar con los módulos del sistema, mientras que Oracle Database almacena la información y ejecuta las reglas principales del negocio mediante funciones, procedimientos, paquetes y triggers.

El desarrollo permite aplicar los temas de Programación III en un caso práctico: clases y objetos, herencia, interfaces, enumeraciones, excepciones, separación de responsabilidades, persistencia y comunicación entre una aplicación Java y una base de datos relacional.

## 2. Planteamiento del problema

### 2.1 Descripción del problema

La administración de habitaciones y reservas requiere mantener actualizada la información de las personas, las habitaciones, los periodos de estancia, los importes y los pagos. Cuando estos datos se registran sin una estructura común o sin validaciones, pueden aparecer duplicidades, errores en las fechas, reservas superpuestas y dificultades para consultar el estado de una operación.

En el contexto académico, se plantea desarrollar un sistema que permita organizar estos procesos mediante una interfaz gráfica y una base de datos relacional. Además de almacenar los registros, el sistema debe aplicar reglas que mantengan la coherencia de las reservas, los comprobantes y los pagos.

### 2.2 Pregunta orientadora

¿Cómo desarrollar una aplicación de escritorio en Java que permita administrar las operaciones principales de un hotel, mantenga la información en una base de datos Oracle y aplique de forma centralizada las reglas de negocio?

### 2.3 Alcance del problema

El proyecto cubre la gestión de huéspedes, trabajadores, habitaciones, reservas, comprobantes, pagos, mantenimiento y reportes. La aplicación se ejecuta como programa de escritorio y se conecta directamente a Oracle mediante JDBC.

La solución puede utilizarse desde varios equipos si estos pueden acceder a la misma instancia Oracle a través de la red y se configuran con las credenciales correspondientes. No incluye una aplicación web, una API intermedia ni integración con pasarelas bancarias o plataformas externas de reservas.

## 3. Justificación

El proyecto permite aplicar conceptos de programación en un sistema con procesos relacionados entre sí. La separación de la interfaz, los servicios, la persistencia y la base de datos facilita organizar el código y asignar responsabilidades a cada componente.

El uso de Oracle permite conservar los registros, establecer relaciones entre las tablas y centralizar reglas que deben cumplirse en las operaciones del hotel. Los procedimientos y funciones PL/SQL se encargan de operaciones como crear reservas, calcular importes, registrar pagos y generar reportes. De esta manera, Java no necesita mantener una segunda implementación de esas reglas.

La integración entre JavaFX, JDBC y Oracle también permite estudiar cómo una aplicación de escritorio puede trabajar con una base de datos centralizada y cómo varios equipos pueden consultar y actualizar la misma información cuando existe conectividad con el servidor.

## 4. Objetivos

### 4.1 Objetivo general

Desarrollar una aplicación de escritorio para gestionar las operaciones principales de un hotel mediante Java, JavaFX y Oracle Database, utilizando una arquitectura organizada y paquetes PL/SQL para centralizar las reglas de negocio.

### 4.2 Objetivos específicos

1. Modelar las entidades principales del sistema, como personas, clientes, trabajadores, habitaciones, reservas y comprobantes.
2. Implementar una interfaz gráfica para registrar, consultar y gestionar la información del hotel.
3. Establecer la comunicación entre Java y Oracle mediante JDBC.
4. Implementar funciones, procedimientos, paquetes y triggers para validar y procesar las operaciones del negocio.
5. Gestionar reservas, disponibilidad, cancelaciones, check-in, check-out, comprobantes y pagos.
6. Generar reportes y estadísticas a partir de la información almacenada en Oracle.
7. Documentar la arquitectura, la instalación, las pruebas y las limitaciones del sistema.

## 5. Estado del arte

### 5.1 Sistemas de gestión hotelera

Los sistemas de gestión hotelera (*Property Management Systems* o PMS) apoyan procesos como la administración de habitaciones, huéspedes, reservas, tarifas y pagos. En soluciones de mayor alcance, estos procesos pueden integrarse con servicios externos, mecanismos de acceso y controles de protección de información.

NIST publicó una guía técnica sobre la seguridad de los sistemas de gestión hotelera que aborda la protección de la información y los controles aplicables a este tipo de plataformas [1]. Este antecedente permite reconocer que un sistema hotelero de producción requiere considerar aspectos adicionales a la gestión básica de reservas.

### 5.2 Antecedentes académicos y técnicos

Vignaga y Perovich Gerosa (2003) presentan una descripción arquitectónica de un subsistema de reservas para un sistema de gestión hotelera. El trabajo aborda objetivos, restricciones, casos de uso y decisiones de diseño, por lo que sirve como referencia para estudiar la organización de este tipo de sistemas [2].

Abduljwad y Wang (2005) describen el diseño de un sistema de reservas hoteleras en línea mediante una arquitectura multinivel. El trabajo destaca la separación entre la interfaz, las reglas de negocio y el almacenamiento de datos [3]. Aunque Hotel Reservas utiliza una aplicación de escritorio y una conexión JDBC directa, la separación de responsabilidades también es un criterio presente en su diseño.

### 5.3 Comparación con Hotel Reservas

| Aspecto | Sistemas hoteleros de mayor alcance | Hotel Reservas |
|---|---|---|
| Tipo de aplicación | Pueden ser soluciones web, móviles o de escritorio | Aplicación de escritorio desarrollada con JavaFX |
| Gestión de reservas | Puede integrarse con canales y servicios externos | Creación, consulta, disponibilidad, cancelación y control de estados |
| Gestión de habitaciones | Inventario y disponibilidad con módulos adicionales | Registro, consulta y mantenimiento de habitaciones |
| Pagos | Pueden conectarse con pasarelas financieras | Registro de comprobantes y pagos; no procesa transacciones bancarias |
| Persistencia | Bases de datos y servicios centralizados | Oracle Database mediante JDBC |
| Acceso desde varios equipos | Depende de la arquitectura y el despliegue | Posible si los equipos acceden a la misma instancia Oracle por la red |
| Finalidad | Apoyar la operación de establecimientos hoteleros | Aplicar y demostrar conceptos de programación y diseño de software |

### 5.4 Brecha y aporte del proyecto

Hotel Reservas no pretende sustituir un PMS comercial. Su aporte académico consiste en integrar una interfaz gráfica, un modelo orientado a objetos, una base de datos relacional y lógica de negocio implementada en PL/SQL.

La solución permite estudiar la comunicación entre Java y Oracle, la distribución de responsabilidades y el uso de procedimientos almacenados para evitar que las mismas reglas estén implementadas de forma duplicada en distintas capas. Su alcance sigue siendo limitado frente a un producto comercial, especialmente en seguridad, integraciones externas, despliegue y administración avanzada.

## 6. Marco teórico

### 6.1 Programación orientada a objetos

La programación orientada a objetos organiza el software mediante clases y objetos que agrupan datos y comportamientos. En Hotel Reservas, las clases representan conceptos del dominio, como `Client`, `Employee`, `Room`, `Reservation` e `Invoice`.

### 6.2 Encapsulamiento y herencia

El encapsulamiento permite controlar el acceso al estado interno de los objetos. La herencia se utiliza para expresar características compartidas entre tipos relacionados. En el modelo, `Client` y `Employee` heredan de `Person`, que contiene los datos comunes de una persona.

### 6.3 Interfaces y polimorfismo

Las interfaces definen contratos que pueden ser implementados por distintas clases. En el proyecto, `Repository<T>` establece operaciones de consulta que utiliza la capa de persistencia. La clase `Person` también permite tratar de forma general a clientes y trabajadores cuando se necesita trabajar con sus atributos comunes.

### 6.4 Excepciones y validación

Las excepciones permiten representar errores sin mezclar su tratamiento con el flujo normal de la aplicación. El proyecto utiliza excepciones para distinguir datos inválidos, incumplimientos de reglas de negocio y recursos inexistentes. Los errores definidos en Oracle se traducen en excepciones Java para que la interfaz pueda informar el resultado de una operación.

### 6.5 Arquitectura por capas

La arquitectura separa el programa en componentes con responsabilidades distintas. JavaFX recibe las entradas y presenta resultados; `HotelService` coordina las solicitudes; la capa JDBC se comunica con Oracle; y la base de datos ejecuta las operaciones de negocio y conserva la información.

### 6.6 Principios GRASP y SOLID

GRASP reúne criterios para asignar responsabilidades a los objetos, mientras que SOLID agrupa principios que orientan el diseño de software mantenible. En Hotel Reservas, la separación entre interfaz, servicio, persistencia y base de datos busca mantener responsabilidades claras y reducir el acoplamiento.

La aplicación de estos principios debe entenderse como una guía de diseño y no como una certificación de cumplimiento total. Cada clase debe evaluarse según las responsabilidades que realmente implementa.

### 6.7 Persistencia y bases de datos relacionales

La persistencia permite conservar la información más allá de la ejecución actual del programa. Hotel Reservas utiliza Oracle Database para almacenar los datos en tablas relacionadas. Las claves primarias y foráneas establecen las relaciones entre registros, mientras que las restricciones ayudan a mantener la integridad de la información.

JDBC permite que Java ejecute consultas y llamadas a procedimientos almacenados. En este proyecto, los repositorios se enfocan en recuperar registros y los cambios de negocio se delegan a los paquetes PL/SQL.

### 6.8 Calidad del software

La norma ISO/IEC 25010:2023 presenta un modelo de calidad de producto que puede utilizarse como referencia para analizar características como adecuación funcional, fiabilidad, seguridad y capacidad de mantenimiento [4]. En este proyecto se considera como marco conceptual; no se afirma que la aplicación haya sido certificada ni evaluada formalmente conforme a la norma.

## 7. Metodología de desarrollo

El desarrollo se organizó de forma incremental. Primero se definieron las entidades y las operaciones principales; posteriormente se construyeron la interfaz, los servicios y la persistencia. En una etapa posterior, la solución se amplió para utilizar JavaFX y Oracle, y se trasladaron las reglas de negocio a paquetes PL/SQL.

El trabajo se apoyó en Maven para administrar la configuración del proyecto y en Git/GitHub para registrar los cambios. La rama `develop` se utiliza como espacio de integración y `main` como rama estable prevista para la entrega.

Las pruebas se realizaron sobre los procedimientos y funciones de Oracle mediante un script de pruebas. Los resultados documentados corresponden a las ejecuciones realizadas durante el desarrollo; no deben interpretarse como una auditoría externa ni como una certificación de calidad.

## 8. Análisis del sistema

### 8.1 Actores y usuarios previstos

El sistema contempla dos roles de trabajadores: `ADMIN` y `RECEPTIONIST`. Ambos pueden acceder a las funciones que les corresponden, mientras que algunas operaciones administrativas, como ciertas tareas de gestión y reportes, requieren un administrador activo.

También se contempla el registro de huéspedes. Un huésped puede registrarse sin intervención de un trabajador y queda clasificado inicialmente como `NEW`. Las operaciones disponibles para huéspedes tienen restricciones diferentes a las de los trabajadores.

### 8.2 Requisitos funcionales

- **RF-01.** Permitir el inicio de sesión de trabajadores registrados y activos.
- **RF-02.** Registrar huéspedes y clientes, validando sus datos.
- **RF-03.** Registrar trabajadores y aplicar las restricciones de rol correspondientes.
- **RF-04.** Registrar y consultar habitaciones.
- **RF-05.** Consultar habitaciones disponibles según fechas y cantidad de huéspedes.
- **RF-06.** Crear reservas y comprobar que las fechas, la capacidad y la disponibilidad sean válidas.
- **RF-07.** Calcular el importe de una reserva de acuerdo con la duración de la estancia y la categoría del cliente.
- **RF-08.** Cancelar reservas respetando las condiciones del sistema y los permisos del usuario.
- **RF-09.** Registrar el check-in y el check-out de las reservas.
- **RF-10.** Generar comprobantes y registrar pagos parciales o completos.
- **RF-11.** Gestionar el estado de mantenimiento de las habitaciones.
- **RF-12.** Consultar reportes, estadísticas y el ranking de habitaciones populares.
- **RF-13.** Conservar los registros en Oracle Database y registrar los cambios de estado de reservas en la tabla de auditoría.

### 8.3 Requisitos no funcionales

- **RNF-01. Mantenibilidad:** organizar el código en paquetes con responsabilidades identificables.
- **RNF-02. Comprensibilidad:** mantener una estructura que pueda revisarse y explicarse durante la sustentación.
- **RNF-03. Persistencia:** conservar la información mediante Oracle Database.
- **RNF-04. Portabilidad del entorno:** permitir compilar y ejecutar el proyecto con el JDK y Maven requeridos y una instancia Oracle accesible.
- **RNF-05. Integridad:** aplicar restricciones y validaciones para reducir inconsistencias en los datos.
- **RNF-06. Conectividad:** permitir que los equipos autorizados se conecten a una misma instancia Oracle cuando la red y el servidor estén configurados para ello.
- **RNF-07. Seguridad limitada:** reconocer que las contraseñas de trabajadores se almacenan en texto plano en esta versión académica y que el sistema requiere mejoras antes de un uso real.

### 8.4 Reglas de negocio

1. Una reserva debe asociarse a un cliente y una habitación existentes.
2. El cliente debe estar activo y, cuando la reserva la registra un trabajador, este también debe estar activo.
3. La fecha de entrada no puede ser anterior al día actual y la fecha de salida debe ser posterior a la entrada.
4. La cantidad de huéspedes no puede superar la capacidad de la habitación.
5. Una habitación no puede tener reservas incompatibles que se crucen en el mismo periodo.
6. Una habitación en mantenimiento o marcada como no disponible no puede reservarse.
7. Las estancias de siete noches o más reciben un descuento del 10 %. La categoría `FREQUENT` aplica un 5 % y la categoría `VIP` un 15 %; los descuentos correspondientes se acumulan según la función de cálculo de Oracle.
8. El check-in solo puede realizarse durante el periodo válido de la reserva y el check-out requiere que la llegada se haya registrado.
9. La cancelación de una reserva está sujeta a su estado. Si existen pagos, la cancelación realizada por un trabajador requiere un administrador; un huésped solo puede cancelar una reserva propia que no tenga pagos.
10. Los abonos deben ser mayores que cero, admitir como máximo dos decimales y no superar el saldo pendiente.
11. No se puede iniciar el mantenimiento de una habitación que tenga reservas activas o con llegada registrada.

## 9. Diseño y arquitectura

### 9.1 Organización por paquetes

La estructura principal del proyecto se organiza en los siguientes componentes:

| Componente | Responsabilidad principal |
|---|---|
| `Main` y `ui.AppFxmlLauncher` | Iniciar la aplicación JavaFX. |
| `ui` | Cargar las ventanas, recibir entradas y presentar resultados. |
| `service` | Coordinar las operaciones solicitadas por los controladores mediante `HotelService`. |
| `persistence` | Gestionar la conexión JDBC, las consultas, los mappers y las llamadas a PL/SQL. |
| `model` | Representar personas, habitaciones, reservas, comprobantes y resultados de reportes. |
| `validation` | Reunir validaciones básicas de entrada. |
| `exceptions` | Representar errores de datos, reglas de negocio y recursos inexistentes. |
| `oracle/` | Contener los scripts SQL y PL/SQL de instalación y pruebas. |

El flujo habitual de una operación es `ui → service → persistence → Oracle`. Los controladores no ejecutan directamente las reglas de negocio ni realizan las escrituras en las tablas.

### 9.2 Entidades y componentes principales

- **Person:** clase base con información común de personas.
- **Client:** representa a un huésped y su categoría (`NEW`, `FREQUENT` o `VIP`).
- **Employee:** representa a un trabajador, su rol y sus credenciales de acceso.
- **Room:** representa una habitación, su tipo, capacidad, tarifa, disponibilidad y mantenimiento.
- **Reservation:** relaciona al cliente, el trabajador que la registra (cuando aplica), la habitación, las fechas, la cantidad de huéspedes, el total y el estado.
- **Invoice:** representa el comprobante asociado a una reserva, su método de pago, monto, abonos, reembolsos y estado.
- **ReportRows:** contiene estructuras de solo lectura para representar los resultados de los reportes recibidos desde Oracle.
- **HotelService:** actúa como fachada entre la interfaz y la persistencia.
- **OracleHotelProcedures:** centraliza las llamadas JDBC a los paquetes PL/SQL.
- **Repository<T>:** define operaciones de consulta para recuperar registros.

### 9.3 Persistencia y base de datos

La información se almacena en Oracle Database. Las tablas principales del sistema son:

| Tabla | Información almacenada |
|---|---|
| `CLIENTES` | Datos de huéspedes y su categoría. |
| `TRABAJADORES` | Datos de trabajadores, rol, usuario, contraseña y estado. |
| `HABITACIONES` | Número, tipo, capacidad, precio, disponibilidad y mantenimiento. |
| `RESERVAS` | Cliente, trabajador opcional, habitación, fechas, huéspedes, precio, total y estado. |
| `COMPROBANTES` | Reserva asociada, fecha de emisión, monto, método de pago, abonos, reembolsos y estado. |
| `AUDITORIA_RESERVAS` | Historial de cambios de estado de las reservas. |

Los paquetes PL/SQL principales son:

- **`PKG_RESERVAS`:** crea y cancela reservas, consulta disponibilidad, calcula totales, registra check-in/check-out, genera comprobantes y registra pagos.
- **`PKG_PERSONAS`:** registra clientes, huéspedes y trabajadores, valida datos y permite iniciar sesión.
- **`PKG_HABITACIONES`:** registra habitaciones y gestiona el mantenimiento.
- **`PKG_REPORTES`:** genera estadísticas generales, reservas por estado, ingresos, uso de habitaciones, mejores clientes, saldos pendientes y ranking de habitaciones.

También se utilizan las funciones `FN_NOCHES` y `FN_HAY_SOLAPE`, además de triggers que validan operaciones relacionadas con las reservas y registran cambios en la auditoría.

Los repositorios Java se utilizan para consultar registros y convertir los resultados en modelos. Las operaciones de escritura y las reglas del negocio se ejecutan mediante los paquetes PL/SQL.

### 9.4 Comunicación entre Java y Oracle

La comunicación se realiza mediante JDBC. `Database` lee el archivo local `db.properties` y abre la conexión. `AppFxml` crea el servicio que utilizará la aplicación. Los controladores llaman a `HotelService`, que prepara los parámetros y delega las operaciones en `OracleHotelProcedures`.

La clase `OracleHotelProcedures` invoca procedimientos y funciones mediante `CallableStatement`. Cuando Oracle devuelve cursores, la capa JDBC lee sus filas y las convierte en modelos o estructuras de reporte. Los errores definidos en PL/SQL se traducen a las excepciones utilizadas por la aplicación.

### 9.5 Conexión desde varias computadoras

La aplicación utiliza una conexión JDBC directa con Oracle. Por esa razón, varios equipos pueden ejecutar su propia copia de Hotel Reservas y conectarse a una misma instancia de Oracle, siempre que exista conectividad de red y el servidor permita las conexiones.

En este escenario, un equipo mantiene activa la instancia Oracle y los demás se conectan utilizando la dirección IP privada del servidor, el puerto del Listener y el nombre del servicio. Cada computadora cliente necesita su propio archivo `db.properties`, configurado para apuntar al mismo esquema de Oracle. No se crea una base de datos independiente en cada equipo ni se ejecutan los scripts de instalación en cada cliente.

La computadora que aloja Oracle debe permanecer encendida y tener activo el Listener. El firewall debe permitir el acceso desde los equipos autorizados. Para conexiones fuera de la red local se recomienda utilizar una VPN y no exponer directamente el puerto de Oracle a Internet.

### 9.6 Principios GRASP y SOLID

| Principio o criterio | Aplicación en el proyecto | Alcance |
|---|---|---|
| GRASP: alta cohesión | La interfaz, el servicio, la persistencia y los paquetes SQL tienen responsabilidades diferenciadas. | Cada componente se concentra en una parte del flujo. |
| GRASP: bajo acoplamiento | Los controladores utilizan `HotelService` y no necesitan ejecutar SQL directamente. | La comunicación con Oracle se concentra en la capa de persistencia. |
| GRASP: experto en información | Oracle aplica las reglas sobre los registros que almacena. | Las operaciones utilizan los datos de las tablas para validar y calcular resultados. |
| SOLID: responsabilidad única (SRP) | Los controladores, servicios, mappers y componentes JDBC tienen responsabilidades distintas. | La separación facilita localizar cambios y errores. |
| SOLID: inversión de dependencias (DIP) | `HotelService` recibe repositorios a través de sus interfaces y utiliza una clase dedicada para las llamadas a Oracle. | La construcción de dependencias se realiza al iniciar el servicio. |

Estas decisiones reflejan criterios de diseño aplicados al proyecto; no constituyen una certificación de cumplimiento total de los principios.

## 10. Tecnologías y herramientas

- **Java 17:** lenguaje utilizado para implementar la aplicación.
- **JavaFX:** biblioteca utilizada para construir la interfaz gráfica.
- **JDBC:** API utilizada para conectar Java con Oracle y ejecutar consultas y procedimientos.
- **Oracle Database:** sistema gestor de base de datos utilizado para almacenar la información.
- **PL/SQL:** lenguaje utilizado para implementar paquetes, procedimientos, funciones y triggers.
- **Apache Maven:** herramienta para administrar dependencias y ejecutar la aplicación.
- **Apache NetBeans:** entorno de desarrollo utilizado durante el proyecto.
- **SQL Developer:** herramienta para conectarse a Oracle, ejecutar scripts y revisar los objetos de la base de datos.
- **Git y GitHub:** herramientas utilizadas para el control de versiones y el alojamiento del repositorio.
- **Markdown y Mermaid:** formatos utilizados para documentación y diagramas, cuando corresponde.

### 10.1 Correspondencia con la guía del proyecto de aula

La documentación presenta el análisis del problema, los objetivos, el diseño, la arquitectura, las tecnologías, las pruebas y las limitaciones del sistema. La versión actual incorpora una interfaz gráfica JavaFX y conexión con una base de datos Oracle, por lo que ya no corresponde describirla como un programa de consola con almacenamiento en archivos de texto.

Los diagramas y las pruebas deben corresponder a la versión del código que se entrega. Los resultados de pruebas se describen según las ejecuciones realizadas y no deben presentarse como comprobaciones que no se hayan llevado a cabo.

## 11. Pruebas y verificación

Durante el desarrollo se ejecutó un script de pruebas PL/SQL para verificar los paquetes de personas, habitaciones, reservas y reportes. Entre los escenarios comprobados se encuentran:

| ID | Escenario de prueba | Resultado esperado |
|---|---|---|
| P-01 | Registrar un cliente con datos válidos | Se registra y los datos se normalizan. |
| P-02 | Repetir documentos, correos o teléfonos | Oracle rechaza los datos duplicados. |
| P-03 | Registrar trabajadores y validar roles | Se aplican las restricciones de autorización. |
| P-04 | Iniciar sesión con credenciales válidas, incorrectas o de un trabajador inactivo | El resultado corresponde al estado de la cuenta y las credenciales. |
| P-05 | Registrar habitaciones con datos válidos o inválidos | Se aceptan los datos correctos y se rechazan los inválidos. |
| P-06 | Calcular importes según noches y categoría | Se aplican los descuentos establecidos. |
| P-07 | Crear reservas válidas e inválidas | Se comprueban fechas, capacidad, cliente, trabajador y disponibilidad. |
| P-08 | Intentar crear reservas superpuestas | Oracle impide los cruces incompatibles. |
| P-09 | Realizar check-in, check-out y cancelaciones | Se respetan los estados y permisos permitidos. |
| P-10 | Generar comprobantes y registrar abonos | Se actualizan los estados y saldos correspondientes. |
| P-11 | Iniciar y finalizar mantenimiento | Se respetan las condiciones de mantenimiento. |
| P-12 | Consultar estadísticas, reportes y rankings | Los cursores devuelven resultados coherentes con los registros. |

En la ejecución realizada durante el desarrollo, el autor informó que el script de pruebas terminó con cero fallos. Este resultado corresponde a las pruebas PL/SQL ejecutadas en el entorno local. No equivale a una auditoría independiente ni demuestra por sí solo que todas las combinaciones de la interfaz JavaFX hayan sido probadas.

La verificación de la aplicación también requiere comprobar la conexión JDBC, el inicio de sesión y las operaciones principales desde JavaFX en el entorno donde se vaya a ejecutar.

## 12. Resultados y aportes

Hotel Reservas integra una interfaz gráfica para gestionar las operaciones principales de un hotel con una base de datos Oracle. La solución permite registrar y consultar información, administrar habitaciones y reservas, registrar comprobantes y pagos, controlar el mantenimiento y consultar reportes.

Uno de los cambios principales de la arquitectura fue trasladar las reglas de negocio a Oracle. Los procedimientos y funciones PL/SQL ejecutan las operaciones y devuelven los resultados que Java presenta en la interfaz. Esto reduce la duplicación de reglas entre la aplicación y la base de datos.

En el aspecto académico, el proyecto reúne conceptos de programación orientada a objetos, herencia, interfaces, manejo de excepciones, JavaFX, JDBC, bases de datos relacionales y programación PL/SQL. También permite estudiar la conexión de varios clientes de escritorio a una misma instancia de Oracle, siempre que se configure la red correspondiente.

## 13. Limitaciones

1. La aplicación es de escritorio y no incluye una interfaz web ni móvil.
2. La comunicación con Oracle se realiza directamente desde cada cliente mediante JDBC; no existe una API o servidor de aplicación intermedio.
3. El acceso desde varios equipos depende de que Oracle, el Listener, la red y el firewall estén configurados correctamente.
4. El registro de pagos es interno al sistema y no representa una integración con una pasarela bancaria real.
5. Las contraseñas de trabajadores se almacenan en texto plano en esta versión académica. Esta decisión limita el uso del sistema fuera de un entorno de pruebas.
6. Las fechas se almacenan como texto en el formato `AAAA-MM-DD`, de acuerdo con el esquema utilizado.
7. La aplicación requiere una instancia Oracle disponible y un archivo `db.properties` configurado en cada equipo cliente.
8. No se incluyen servicios de reserva por Internet ni integración con plataformas externas.
9. El sistema no debe utilizarse como producto comercial de producción sin ampliar las pruebas, la seguridad, los respaldos, la recuperación ante fallos y la administración de usuarios y conexiones.

Si se almacenan datos personales reales, deben revisarse las obligaciones aplicables de protección de datos. En Colombia, la Ley Estatutaria 1581 de 2012 establece disposiciones generales para el tratamiento de datos personales [5]. Esta mención es contextual y no constituye una evaluación jurídica ni una afirmación de cumplimiento legal del proyecto.

## 14. Trabajo futuro

- Mejorar el almacenamiento de contraseñas mediante mecanismos criptográficos adecuados.
- Incorporar pruebas automatizadas para los flujos de JavaFX y la integración JDBC.
- Evaluar una API o servidor de aplicación intermedio si el sistema requiere despliegues más amplios.
- Mejorar el manejo de fechas y los tipos de datos de la base de datos.
- Ampliar la gestión de respaldos y recuperación ante errores.
- Incorporar mecanismos de configuración y despliegue más sencillos para distintos entornos.
- Ampliar los reportes y las consultas de acuerdo con las necesidades del hotel.

## 15. Conclusiones

Hotel Reservas presenta una solución académica para organizar las operaciones principales de un sistema de gestión hotelera. La aplicación combina JavaFX para la interfaz, Java y JDBC para la comunicación y Oracle Database para el almacenamiento y la ejecución de las reglas de negocio.

La integración de paquetes PL/SQL permitió centralizar validaciones, cálculos y operaciones de escritura, evitando mantener implementaciones duplicadas entre Java y Oracle. La separación entre controladores, servicio, persistencia y base de datos permite identificar las responsabilidades principales de cada componente.

El proyecto aplica los conceptos estudiados en Programación III en un caso práctico que puede explicarse desde su modelo de datos, su arquitectura y sus procesos principales. Aunque mantiene limitaciones propias de su alcance académico, constituye una base funcional para estudiar el desarrollo de aplicaciones Java conectadas a una base de datos relacional.

## Referencias

[1] Newhouse, B., Ekstrom, M., Finke, J. y Harriston, M. (2021). *Securing Property Management Systems*. NIST Special Publication 1800-27. National Institute of Standards and Technology. https://doi.org/10.6028/NIST.SP.1800-27

[2] Vignaga, A. y Perovich Gerosa, D. (2003). *SAD del subsistema de reservas del sistema de gestión hotelera*. Reportes Técnicos 03-15, Universidad de la República, Facultad de Ingeniería, Instituto de Computación. https://hdl.handle.net/20.500.12008/3499

[3] Abduljwad, F. y Wang, G.-J. (2005). *Design and Implementation of an Online Hotel Booking System Using Multi-Tier Architecture*. Journal of Jishou University (Natural Science Edition), 26(1).

[4] International Organization for Standardization. (2023). *ISO/IEC 25010:2023 — Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE) — Product quality model*. https://www.iso.org/standard/78176.html

[5] Congreso de Colombia. (2012). *Ley Estatutaria 1581 de 2012, por la cual se dictan disposiciones generales para la protección de datos personales*. https://www.cancilleria.gov.co/sites/default/files/Normograma/docs/ley_1581_2012.htm
