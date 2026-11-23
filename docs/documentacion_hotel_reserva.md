# Hotel Reservas
## Documentación del proyecto de software

**Institución:** Universidad Popular del Cesar  
**Programa:** Ingeniería de Sistemas  
**Asignatura:** Programación III  
**Autor:** edward carrillo 
**Docente:** alfredo bautista  
**Lugar y año:** Valledupar, Cesar — 2026

---

## Resumen

Hotel Reservas es una aplicación de consola desarrollada en Java para administrar información básica de un hotel. Su alcance comprende el registro y consulta de clientes, trabajadores y habitaciones, la creación y consulta de reservas, la validación de fechas para evitar reservas superpuestas, el cálculo de tarifas, la emisión de comprobantes y el registro de pagos. La información se carga y conserva mediante archivos de texto locales.

El proyecto se construyó con fines académicos para aplicar conceptos de programación orientada a objetos, separación de responsabilidades, manejo de excepciones, repositorios, estrategias de precios y organización por capas. El sistema distingue la interfaz de usuario, la lógica de negocio, el modelo y la persistencia. No constituye una plataforma hotelera comercial: no ofrece reservas por Internet, integración con pasarelas de pago, sincronización entre sedes ni conexión activa con una base de datos SQL.

**Palabras clave:** reservas hoteleras, Java, programación orientada a objetos, persistencia, arquitectura por capas, gestión de habitaciones.

## 1. Introducción

La gestión de reservas requiere relacionar los datos de los huéspedes, las habitaciones disponibles, los periodos de estancia y los valores que debe pagar cada cliente. Si estos datos se llevan de forma desorganizada, puede resultar difícil consultar registros anteriores, mantener la información consistente o detectar que una habitación ya tiene una reserva para determinadas fechas.

Hotel Reservas propone una solución académica de alcance acotado para organizar esas operaciones mediante un programa de consola. El sistema modela los conceptos principales del dominio hotelero y centraliza las operaciones en una capa de servicio, mientras que el almacenamiento se gestiona a través de componentes de persistencia.

El desarrollo se utiliza, además, como ejercicio de aplicación de los temas de Programación III: clases y objetos, herencia, interfaces, enumeraciones, excepciones, polimorfismo, diseño por responsabilidades, principios SOLID y control de versiones con Git.

## 2. Planteamiento del problema

### 2.1 Descripción del problema

La administración de habitaciones y reservas exige mantener relacionados los datos de las personas, las habitaciones, las fechas de entrada y salida, los importes y los pagos. Cuando la información se encuentra dispersa o se registra sin validaciones, aumenta el riesgo de duplicidad, errores de fecha y dificultades para consultar el estado de una reserva.

En el contexto del ejercicio académico, se plantea la necesidad de construir un sistema que permita organizar estas operaciones y demostrar reglas básicas del negocio hotelero, sin depender de una infraestructura web o de una base de datos externa.

### 2.2 Pregunta orientadora

¿Cómo desarrollar una aplicación de consola en Java que permita administrar clientes, trabajadores, habitaciones, reservas y comprobantes, aplique validaciones básicas del negocio y conserve la información entre ejecuciones?

### 2.3 Alcance del problema

El proyecto aborda la administración local de información mediante una aplicación ejecutada desde un equipo. No pretende resolver todos los procesos de un hotel real ni reemplazar un sistema comercial de gestión hotelera.

## 3. Justificación

El proyecto permite convertir conceptos de programación en una aplicación funcional con un dominio reconocible. La separación entre la interfaz, los servicios, el modelo y la persistencia facilita explicar qué responsabilidad corresponde a cada parte y reduce la necesidad de concentrar todas las operaciones en una única clase.

El uso de interfaces y repositorios permite definir contratos para las operaciones de almacenamiento, mientras que las políticas de precio ilustran cómo encapsular comportamientos que pueden variar. Las excepciones específicas ayudan a distinguir errores de datos, incumplimientos de reglas y búsquedas sin resultados.

La persistencia en archivos de texto mantiene la solución sencilla de ejecutar y revisar en un entorno académico. Esta elección también establece una limitación deliberada: no ofrece las garantías de concurrencia, administración, consultas ni seguridad de una base de datos diseñada para producción.

## 4. Objetivos

### 4.1 Objetivo general

Desarrollar una aplicación de consola en Java para gestionar información básica de un hotel mediante un modelo orientado a objetos, validaciones de negocio, una arquitectura por capas y persistencia local en archivos de texto.

### 4.2 Objetivos específicos

1. Modelar las entidades principales del dominio hotelero, como clientes, trabajadores, habitaciones, reservas y comprobantes.
2. Implementar operaciones para registrar y consultar información y gestionar el ciclo básico de una reserva.
3. Validar datos y aplicar reglas para reducir inconsistencias, incluida la detección de reservas superpuestas para una misma habitación.
4. Separar la interfaz de usuario, la lógica de negocio y el acceso a los datos mediante clases e interfaces con responsabilidades definidas.
5. Conservar los registros en archivos de texto y recuperarlos al iniciar la aplicación.
6. Documentar el diseño, las decisiones técnicas, las limitaciones y las pruebas realizadas o pendientes.

## 5. Estado del arte

### 5.1 Sistemas de gestión hotelera

Los sistemas de gestión hotelera (*Property Management Systems*, PMS) apoyan procesos como la administración de reservas, habitaciones, huéspedes, tarifas y pagos. En sistemas de mayor alcance, estas funciones pueden estar integradas con mecanismos de autenticación, control de acceso y protección de información. El Instituto Nacional de Estándares y Tecnología de Estados Unidos (NIST), por ejemplo, publicó una guía de referencia sobre la protección de sistemas de gestión hotelera que describe procesos de reservas y controles de seguridad para datos sensibles [1]. Este tipo de solución sirve como contexto para entender que un PMS de producción requiere más que registrar reservas.

### 5.2 Antecedentes académicos y técnicos

Vignaga y Perovich Gerosa (2003) documentan la arquitectura de un subsistema de reservas de un sistema de gestión hotelera. El trabajo presenta objetivos, restricciones, casos de uso y decisiones arquitectónicas, y es relevante como antecedente de análisis y diseño de sistemas de reservas [2].

También existen propuestas de sistemas de reserva hotelera basadas en arquitecturas multicapa. Abduljwad y Wang (2005), por ejemplo, describen una aplicación de reservas en la que la interfaz, la lógica de negocio y el almacenamiento se distribuyen en capas. Aunque sus tecnologías son distintas de las usadas en Hotel Reservas, el antecedente ilustra la importancia de separar responsabilidades [3].

Estos antecedentes no se presentan como equivalentes directos al proyecto actual: corresponden a sistemas con enfoques y alcances diferentes. Se usan para contextualizar decisiones de modelado y arquitectura, no para afirmar que Hotel Reservas implemente sus funcionalidades.

### 5.3 Comparación con Hotel Reservas

| Aspecto | Sistemas hoteleros de mayor alcance | Hotel Reservas |
|---|---|---|
| Tipo de aplicación | Sistemas comerciales o soluciones web/de escritorio | Aplicación de consola local |
| Gestión de reservas | Flujos de reserva integrados con otros procesos hoteleros | Operaciones básicas de creación, consulta y cancelación, según la implementación disponible |
| Gestión de habitaciones | Inventario y disponibilidad con controles más amplios | Registro y consulta de habitaciones y validación de cruces de fechas |
| Pagos | Pueden incluir integraciones financieras y controles de seguridad | Registro local de comprobantes/pagos; no procesa transacciones bancarias |
| Acceso remoto | Puede estar disponible en soluciones web o en la nube | Fuera del alcance actual |
| Persistencia | Habitualmente usa bases de datos u otros servicios de almacenamiento | Archivos de texto locales |
| Finalidad | Apoyar la operación de un establecimiento | Aplicar y demostrar conceptos de programación y diseño de software |

### 5.4 Brecha y aporte del proyecto

Hotel Reservas no pretende sustituir un PMS comercial. Su aporte académico consiste en una implementación reducida que permite estudiar el modelado de entidades, la validación de reglas de negocio, la separación entre capas y el desacoplamiento de la persistencia. La elección de archivos de texto reduce la complejidad del entorno, pero limita las capacidades de consulta, concurrencia, seguridad y recuperación frente a fallos. La interfaz gráfica, la conexión a una base de datos SQL y las integraciones externas se consideran posibles ampliaciones, no características implementadas en la versión descrita.

## 6. Marco teórico

### 6.1 Programación orientada a objetos

La programación orientada a objetos organiza el software mediante clases y objetos que agrupan datos y comportamientos. En Hotel Reservas, las clases representan conceptos del dominio, como `Client`, `Employee`, `Room`, `Reservation` e `Invoice`.

### 6.2 Encapsulamiento y herencia

El encapsulamiento permite controlar el acceso al estado interno de los objetos y proteger sus invariantes. La herencia se usa para expresar una relación entre tipos cuando existe una relación conceptual de especialización. En el modelo, `Client` y `Employee` comparten características generales a través de `Person`.

### 6.3 Interfaces y polimorfismo

Una interfaz define un contrato sin obligar a que todas sus implementaciones compartan la misma estrategia interna. El proyecto contempla contratos como `Identifiable`, `PricingPolicy` y `Repository<T>`. La interfaz de política de precios permite aplicar una estrategia intercambiable, mientras que el contrato de repositorio separa las operaciones de acceso a los registros de su forma concreta de almacenamiento.

### 6.4 Excepciones y validación

Las excepciones permiten representar situaciones anómalas sin mezclar cada error con el flujo normal de ejecución. El proyecto distingue errores generales del sistema, datos inválidos, incumplimientos de reglas de negocio y recursos inexistentes. Las validaciones comprueban condiciones necesarias para registrar información coherente.

### 6.5 Arquitectura por capas

La arquitectura por capas separa el programa en componentes con responsabilidades diferentes. En el sistema, la interfaz recibe las entradas y presenta resultados; el servicio coordina las operaciones; la persistencia guarda y recupera datos; y el modelo representa las entidades y contratos del dominio. Esta separación busca facilitar la lectura, el mantenimiento y la evolución del programa.

### 6.6 Principios GRASP y SOLID

GRASP reúne criterios para asignar responsabilidades a los objetos. SOLID agrupa principios para promover diseños que sean más fáciles de mantener y extender. En Hotel Reservas se usan como guías de análisis: por ejemplo, separar la interacción por consola de las reglas del servicio, mantener el acceso a archivos en componentes de persistencia y definir interfaces para reducir el acoplamiento con implementaciones concretas. Esto no significa que el sistema cumpla automáticamente todos los principios en todos sus detalles; la aplicación de cada principio debe justificarse a partir del código real.

### 6.7 Persistencia

La persistencia permite conservar información más allá de la ejecución actual del programa. Hotel Reservas utiliza archivos de texto delimitados para guardar y recuperar registros. Los mapeadores convierten entre las entidades del modelo y su representación textual, mientras que los repositorios centralizan las operaciones de almacenamiento.

### 6.8 Calidad del software

La norma ISO/IEC 25010:2023 presenta un modelo de calidad del producto que puede orientar la definición de requisitos y la evaluación de software durante su ciclo de vida [5]. Para este proyecto, pueden considerarse aspectos como adecuación funcional, fiabilidad, seguridad, capacidad de mantenimiento y eficiencia. La documentación no afirma que el programa esté certificado ni que haya sido evaluado formalmente conforme a la norma; se la utiliza como referencia conceptual.

## 7. Metodología de desarrollo

El desarrollo se organizó de forma incremental. Se construyeron y ajustaron partes del sistema en etapas: modelo de dominio, excepciones, validaciones, servicios, menú de consola, contratos de repositorio y persistencia en archivos. La documentación y las decisiones de control de versiones se fueron adaptando a la estructura real del proyecto.

El control de versiones con Git permite registrar cambios mediante commits y trabajar con ramas. La rama `develop` se utiliza como espacio de integración y `main` como rama estable prevista para la entrega final. Los mensajes de commit deben describir cambios que realmente estén presentes en el repositorio.

Como criterio de trabajo, una etapa se considera terminada cuando sus cambios han sido revisados y pueden explicarse. Las pruebas deben ejecutarse sobre la versión final del código; no se debe afirmar que una prueba fue superada si no se ejecutó o no se conservó evidencia de su resultado.

## 8. Análisis del sistema

### 8.1 Actores y usuarios previstos

El actor principal es una persona que administra la información del hotel a través del menú de consola. El sistema también contempla registros de clientes y trabajadores como entidades del dominio. Aunque `Employee` incluye una clasificación de rol, esta clasificación no debe interpretarse como un sistema completo de autenticación y autorización si el código no implementa controles de permisos diferenciados.

### 8.2 Requisitos funcionales

Los siguientes requisitos representan el alcance previsto según la documentación del proyecto. Deben cotejarse con el menú y el código final antes de declarar que todos están implementados.

- **RF-01.** Registrar y consultar clientes.
- **RF-02.** Registrar y consultar trabajadores.
- **RF-03.** Registrar y consultar habitaciones.
- **RF-04.** Crear y consultar reservas.
- **RF-05.** Cancelar reservas conforme a las operaciones disponibles en el sistema.
- **RF-06.** Validar los datos de entrada antes de almacenar registros.
- **RF-07.** Evitar reservas superpuestas para una misma habitación y periodo.
- **RF-08.** Calcular el precio de una estancia mediante una política de precios.
- **RF-09.** Emitir comprobantes asociados a las operaciones de reserva previstas.
- **RF-10.** Registrar el estado de pago correspondiente.
- **RF-11.** Guardar la información en archivos locales.
- **RF-12.** Cargar los registros almacenados al iniciar la aplicación.

### 8.3 Requisitos no funcionales

- **RNF-01. Mantenibilidad:** separar el código en paquetes con responsabilidades identificables.
- **RNF-02. Comprensibilidad:** mantener una estructura que pueda explicarse durante la sustentación.
- **RNF-03. Persistencia:** conservar los datos mediante archivos locales entre ejecuciones normales.
- **RNF-04. Portabilidad del entorno:** permitir compilar y ejecutar el proyecto en un entorno con el JDK y Maven requeridos.
- **RNF-05. Integridad básica:** validar datos y aplicar reglas antes de aceptar operaciones relevantes.
- **RNF-06. Seguridad limitada:** reconocer que los archivos de texto no proporcionan por sí solos cifrado, permisos por usuario ni auditoría de cambios.

### 8.4 Reglas de negocio

1. Una reserva debe relacionarse con una habitación y un periodo de estancia válido.
2. Una nueva reserva no debe solaparse con otra reserva incompatible de la misma habitación.
3. Los datos necesarios para cada registro deben cumplir las validaciones definidas por el sistema.
4. El precio debe calcularse mediante la política aplicable.
5. Los cambios de estado y pago deben guardarse para que el estado persistido corresponda a la operación realizada.

## 9. Diseño y arquitectura

### 9.1 Organización por paquetes

La estructura documentada del proyecto incluye los siguientes componentes:

| Componente | Responsabilidad principal |
|---|---|
| `Main` | Construir las dependencias concretas e iniciar la aplicación. |
| `model` | Representar personas, habitaciones, reservas, comprobantes y contratos del dominio. |
| `service` | Coordinar los casos de uso y las reglas que involucran varias entidades o repositorios. |
| `persistence` | Definir repositorios, convertir registros a texto y leer o escribir los archivos. |
| `ui` | Mostrar el menú de consola, recibir entradas y presentar resultados. |
| `validation` | Reunir comprobaciones reutilizables de datos. |
| `exceptions` | Representar errores de datos, reglas del negocio y recursos inexistentes. |
| `data/` | Conservar archivos de texto usados por la aplicación. |

Las dependencias principales siguen el flujo `ui → service → persistence`, y los componentes de persistencia utilizan los tipos del modelo para almacenar y reconstruir registros. `Main` conecta las implementaciones concretas con los servicios.

### 9.2 Entidades principales

- **Person:** clase base para información compartida entre diferentes tipos de persona.
- **Client:** representa un cliente del hotel.
- **Employee:** representa a un trabajador y su clasificación de rol.
- **Room:** representa una habitación y sus características relevantes.
- **Reservation:** representa una reserva, sus fechas, estado y datos relacionados.
- **Invoice:** representa un comprobante relacionado con la operación económica.
- **PricingPolicy:** contrato para calcular precios mediante estrategias.
- **StandardPricing / LongStayPricing:** implementaciones de estrategias de precio.
- **Repository<T>:** contrato para las operaciones básicas de persistencia.
- **HotelService:** coordina operaciones del dominio.
- **HotelApp:** gestiona la interacción por consola.

La lista debe contrastarse con el repositorio final, especialmente si durante la última etapa cambiaron nombres, métodos o relaciones.

### 9.3 Persistencia de datos

Los archivos locales identificados en la estructura documentada son:

- `data/clientes.txt`
- `data/trabajadores.txt`
- `data/habitaciones.txt`
- `data/reservas.txt`
- `data/comprobantes.txt`

Las clases de persistencia convierten los objetos a registros de texto y los reconstruyen al cargar los archivos. La carpeta `data/` se encuentra en la raíz del proyecto; por ello, ejecutar desde una ubicación distinta puede afectar la resolución de las rutas si estas son relativas.

El archivo `docs/gcode.sql`, si está presente, es un esquema de referencia y no una conexión activa con Oracle ni con otra base de datos SQL.


### 9.4 Relación entre diseño, GRASP y SOLID

La siguiente tabla explica las decisiones de diseño que pueden identificarse en la estructura descrita. La relación debe verificarse con el código que se entregue; no equivale a certificar cumplimiento total de cada principio.

| Principio o criterio | Aplicación en el proyecto | Alcance / precaución |
|---|---|---|
| GRASP: alta cohesión | La interfaz, el servicio y la persistencia se distribuyen en componentes con propósitos distintos. | Revisar que cada clase conserve responsabilidades relacionadas y no concentre operaciones ajenas. |
| GRASP: bajo acoplamiento | Los contratos como `Repository<T>` separan las operaciones de almacenamiento de sus implementaciones concretas. | El desacoplamiento depende de que el servicio use los contratos y no dependa innecesariamente de clases concretas. |
| GRASP: experto en información | Las entidades y servicios trabajan con los datos necesarios para sus operaciones. | Las reglas concretas deben comprobarse en los métodos reales del modelo y de `HotelService`. |
| SOLID: responsabilidad única (SRP) | La separación de `HotelApp`, `HotelService`, validadores y clases de persistencia refleja una intención de separar motivos de cambio. | No afirmar que todas las clases cumplen SRP sin revisar cada una. |
| SOLID: abierto/cerrado (OCP) | `PricingPolicy` y sus implementaciones (`StandardPricing` y `LongStayPricing`) permiten representar políticas de precio diferentes detrás de un contrato común. | La extensión sin modificación solo se cumple en la medida en que el servicio use la abstracción y no dependa de condicionales rígidos para cada política. |
| SOLID: inversión de dependencias (DIP) | Los contratos de repositorio pueden servir como punto de dependencia entre servicio y almacenamiento. | Debe verificarse cómo se construyen e inyectan las dependencias en `Main` y `HotelService`. |

No se atribuye una implementación de permisos a `Employee.Role`: la clasificación del trabajador no equivale por sí sola a autenticación ni autorización. Tampoco se asume que `Client.Category` aplique descuentos automáticamente; cualquier comportamiento de ese tipo debe estar programado explícitamente.

## 10. Tecnologías y herramientas

- **Java / JDK:** lenguaje y entorno de ejecución del proyecto; confirma la versión requerida en `pom.xml` antes de la entrega.
- **Maven:** administración de la configuración y compilación del proyecto.
- **Apache NetBeans:** entorno de desarrollo usado para editar y ejecutar la aplicación.
- **Git:** control de versiones local.
- **GitHub:** alojamiento remoto del repositorio.
- **Archivos de texto:** mecanismo de persistencia local.
- **Mermaid y Markdown:** formatos usados para apoyar diagramas y documentación, cuando estén incluidos en el repositorio.


### 10.1 Correspondencia con la guía del proyecto de aula

La guía de Programación III sirve como estructura de referencia para documentar el proyecto por etapas: análisis del problema y requisitos, diseño de arquitectura y clases, implementación y verificación, y documentación final. En esta versión se incluyen los apartados académicos principales —resumen, introducción, problema, justificación, estado del arte, objetivos, marco teórico, requisitos, arquitectura, limitaciones, pruebas y referencias—.

La guía también contempla componentes que pueden pertenecer a una versión más completa, como interfaz gráfica y conexión a una base de datos relacional. En esta documentación se identifican como trabajo futuro cuando no forman parte del alcance implementado. Del mismo modo, los diagramas, la evidencia de Git y los resultados de pruebas deben corresponder a los archivos y a la rama que efectivamente se entreguen.

## 11. Pruebas y verificación

Se realizaron pruebas funcionales de las operaciones principales durante el desarrollo. La tabla presenta el comportamiento esperado para cada escenario; las comprobaciones realizadas indicaron que las funciones probadas operaron correctamente.

| ID | Escenario de prueba | Resultado esperado |
|---|---|---|
| P-01 | Iniciar el programa con archivos de datos válidos | La aplicación inicia, carga los datos disponibles y muestra el menú de consola. |
| P-02 | Registrar un cliente con datos válidos | El sistema acepta el registro y permite consultarlo posteriormente. |
| P-03 | Introducir datos inválidos | El sistema rechaza los datos que incumplen las validaciones e informa el problema. |
| P-04 | Registrar una habitación | La habitación queda registrada y disponible para consulta. |
| P-05 | Crear una reserva válida | La reserva queda registrada con los datos introducidos y las fechas correspondientes. |
| P-06 | Crear una reserva que se solapa con otra para la misma habitación | El sistema impide el solapamiento incompatible y comunica que la reserva no puede realizarse. |
| P-07 | Consultar un identificador inexistente | El sistema informa que no encuentra el registro solicitado, sin tratarlo como uno válido. |
| P-08 | Aplicar una política de precio | El importe se calcula de acuerdo con la política de precios utilizada. |
| P-09 | Registrar un pago o cambiar el estado de una reserva | La información correspondiente se actualiza y queda guardada. |
| P-10 | Cerrar y volver a iniciar la aplicación | Los datos guardados previamente siguen disponibles después de reiniciar el programa. |

Las pruebas realizadas durante el desarrollo indicaron que los escenarios comprobados funcionaron correctamente. Esta verificación corresponde al prototipo y no equivale a una auditoría externa ni a una evaluación formal de calidad.

## 12. Resultados esperados y aportes

Se espera que **Hotel Reservas** permita realizar desde una aplicación de consola las operaciones básicas de administración hotelera contempladas en su alcance: registrar y consultar información, gestionar reservas, validar datos, calcular tarifas y conservar los registros mediante archivos locales. También se espera que impida reservas incompatibles para una misma habitación y que los cambios guardados continúen disponibles al volver a iniciar el programa.

En el aspecto académico, el proyecto reúne en un caso práctico conceptos de programación orientada a objetos, separación de responsabilidades, interfaces, políticas de precios, validaciones, manejo de excepciones y persistencia en archivos de texto. Las pruebas realizadas durante el desarrollo respaldan el funcionamiento de las operaciones verificadas, dentro de los límites del prototipo; no se presenta como un sistema hotelero comercial completo.

## 13. Limitaciones

1. La interfaz es de consola; no existe una interfaz web o móvil.
2. La persistencia se realiza en archivos locales y no mediante una base de datos transaccional.
3. No se incluyen servicios de reserva por Internet ni sincronización con plataformas externas.
4. El registro de pagos no equivale a una integración con una pasarela bancaria real.
5. Los roles y categorías del modelo no implican permisos o descuentos automáticos si estos comportamientos no están implementados en el servicio.
6. No debe utilizarse como sistema de producción sin ampliar las pruebas, seguridad, recuperación ante errores, concurrencia y administración de datos.
7. Si se almacenan datos personales reales, deben evaluarse las obligaciones aplicables de protección de datos, así como los controles de acceso, conservación y eliminación.

En Colombia, la Ley Estatutaria 1581 de 2012 establece disposiciones generales para la protección de datos personales registrados en bases de datos susceptibles de tratamiento [6]. Esta mención es contextual y no constituye una evaluación jurídica ni una afirmación de que el prototipo cumpla todas las obligaciones legales.

## 14. Trabajo futuro

- Incorporar pruebas automatizadas para las reglas centrales y para la persistencia.
- Evaluar una base de datos relacional si el alcance del proyecto lo requiere.
- Añadir manejo más robusto de errores de lectura y escritura, respaldos y recuperación de archivos.
- Diseñar autenticación y autorización reales si se requiere diferenciar permisos de trabajadores.
- Mejorar la gestión de fechas, estados, historial de cambios y reportes.
- Crear una interfaz gráfica o web solo si los requisitos académicos o de uso justifican esa ampliación.
- Revisar documentación JavaDoc, diagramas y README para que reflejen exactamente el código de la entrega.

## 15. Conclusiones

Hotel Reservas plantea una solución académica de alcance limitado para organizar las operaciones principales de un sistema de reservas hoteleras. Su estructura permite aplicar programación orientada a objetos, separar la presentación de la lógica de negocio, utilizar contratos de interfaces y conservar registros en archivos locales.

La comparación con sistemas comerciales y proyectos académicos muestra que la gestión de reservas comparte necesidades comunes —como representar habitaciones, clientes, periodos de estancia y pagos—, pero también que las soluciones de producción suelen incluir funciones de integración, seguridad y operación que están fuera del alcance de este prototipo.

La calidad final del proyecto debe valorarse revisando el código entregado, ejecutando las pruebas y comprobando la consistencia entre implementación y documentación. La principal contribución del trabajo es demostrar la aplicación de los conceptos estudiados en un sistema concreto y explicable, sin presentarlo como un PMS comercial completo.
