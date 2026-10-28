# Diseño del Sistema Hotel Reservas

## Sobre las personas del sistema

**¿Qué atributos son comunes a todas las personas que interactúan con el hotel?**

Las personas comparten identificador, nombre, número de documento, correo electrónico, teléfono, ciudad y estado de actividad. Estos datos están definidos en la clase abstracta `Person`.

**¿Cuáles son propios de cada tipo específico de persona?**

- **Client:** categoría del cliente (`NEW`, `FREQUENT` o `VIP`).
- **Employee:** rol (`ADMIN` o `RECEPTIONIST`), nombre de usuario y contraseña.

Actualmente, la categoría del cliente y el rol del trabajador se almacenan como datos. No se aplican automáticamente descuentos por categoría ni permisos diferentes por rol.

**¿Cómo se refleja esta distinción en una jerarquía de clases?**

`Client` y `Employee` heredan de `Person`. La clase base mantiene los atributos compartidos y cada subclase incorpora los datos y comportamientos que le corresponden.

**¿Debería existir una clase que represente a una persona genérica sin especificar su tipo?**

Sí. `Person` es abstracta porque representa los elementos comunes de las personas del hotel, pero el sistema crea clientes y trabajadores concretos.

**¿Qué implica que `Person` sea abstracta?**

No se puede instanciar directamente con `new Person(...)`. Se crean objetos de `Client` o `Employee`. Además, `Person` declara `describe()` como abstracto, por lo que cada subclase debe proporcionar su propia descripción. Esto permite utilizar el polimorfismo al recorrer una lista de objetos `Person`.

---

## Sobre las habitaciones

**¿Qué información representa una habitación?**

La clase `Room` contiene identificador, número de habitación, tipo (`SINGLE`, `DOUBLE` o `SUITE`), capacidad, precio por noche y un indicador de disponibilidad.

**¿Por qué el tipo de habitación se representa con un `enum`?**

`Room.Type` establece las opciones válidas para el tipo de habitación. Esto evita que se registren variantes escritas de manera inconsistente, como `suite`, `Suite` o `Suit`.

**¿Por qué la habitación tiene un precio por noche?**

El precio pertenece a la información propia de la habitación y sirve como base para calcular el costo de una estancia. La tarifa final de una reserva se guarda también en `Reservation`, porque debe conservar el importe calculado cuando se creó la reserva.

**¿Cómo se representa la disponibilidad?**

`Room` tiene los métodos `markAvailable()` y `markUnavailable()` para cambiar su indicador de disponibilidad. Además, al crear una reserva, el servicio revisa las reservas activas para impedir que dos reservas ocupen la misma habitación durante fechas superpuestas. Por eso el indicador de disponibilidad no reemplaza la comprobación de fechas.

---

## Sobre las reservas y sus relaciones con otras entidades

Una reserva relaciona a un cliente, una habitación, un periodo de estancia y, si hay un trabajador disponible en los datos, un empleado asociado.

**¿Qué atributos principales tiene `Reservation`?**

La clase guarda identificador, cliente, empleado asociado, habitación, fecha de entrada, fecha de salida, precio por noche, total y estado (`ACTIVE` o `CANCELLED`).

**¿Qué relaciones existen entre `Reservation` y las demás clases?**

- `Reservation` — `Client`: asociación. La reserva identifica al cliente que la solicita.
- `Reservation` — `Room`: asociación. La reserva identifica la habitación que se ocupa durante el periodo.
- `Reservation` — `Employee`: asociación opcional en la implementación actual. El servicio vincula al primer trabajador registrado; si no existe ninguno, la referencia puede ser nula.
- `Invoice` — `Reservation`: asociación. El comprobante pertenece a una reserva, pero la reserva puede seguir existiendo aunque el comprobante se anule.

Estas relaciones no son herencia. Tampoco se modelan como composición estricta: el cliente, el trabajador y la habitación tienen identidad y pueden existir antes y después de una reserva.

**¿Quién crea una reserva nueva y cómo calcula el total?**

El método estático `Reservation.create(...)` construye una reserva usando el cliente, la habitación, las fechas y una `PricingPolicy`. Calcula las noches entre la entrada y la salida y aplica la política de precio recibida.

**¿Qué validaciones realiza la reserva al crearse?**

Comprueba que existan los datos obligatorios, que la fecha de salida sea posterior a la de entrada, que el cliente esté activo y que la habitación esté marcada como disponible. El servicio también compara la solicitud con las reservas existentes y rechaza los periodos superpuestos para la misma habitación.

**¿Cómo se determina si dos reservas se superponen?**

El método `Reservation.occupies(...)` comprueba que la reserva existente esté activa, que se refiera a la misma habitación y que los periodos de fechas se crucen. Las reservas canceladas no bloquean la habitación.

**¿Cómo se cancela una reserva?**

El método `Reservation.cancel()` cambia el estado de `ACTIVE` a `CANCELLED`. Si se intenta cancelar una reserva ya cancelada, lanza `BusinessRuleException`. Después, `HotelService` solicita al repositorio actualizar el registro para que el cambio se guarde.

---

## Sobre los comprobantes y los pagos

**¿Qué información almacena `Invoice`?**

El comprobante contiene identificador, reserva asociada, fecha de emisión, importe, método de pago (`CASH`, `CARD` o `TRANSFER`) y estado (`PENDING`, `PAID` o `VOIDED`).

**¿Cómo se calcula el importe de un comprobante?**

El método `Invoice.issue(...)` utiliza el total de la reserva. El usuario selecciona el método de pago, pero no escribe manualmente el importe, lo que evita que el comprobante use un monto diferente del de la reserva.

**¿Qué reglas se aplican al emitir un comprobante?**

Solo se puede emitir para una reserva activa. El comprobante nuevo se crea con la fecha del día y estado `PENDING`.

**¿Cómo se registra un pago?**

`Invoice.pay()` permite cambiar el estado de `PENDING` a `PAID`. Si el comprobante no está pendiente, la operación se rechaza mediante `BusinessRuleException`. `HotelService.payInvoice(...)` localiza el comprobante, realiza la operación y actualiza el repositorio para persistir el cambio.

**¿Para qué sirve el estado `VOIDED`?**

Representa un comprobante anulado. `Invoice.annul()` evita anular de nuevo uno que ya está anulado. En el menú actual, las operaciones expuestas son emitir, listar y registrar el pago de comprobantes; no se presenta una opción independiente para anularlos.

---

## Sobre las políticas de precio

**¿Por qué el cálculo de las tarifas se separa en distintas clases?**

El sistema define la interfaz `PricingPolicy`, que declara `calculateTotal(pricePerNight, nights)`. Las clases `StandardPricing` y `LongStayPricing` implementan ese contrato con cálculos distintos.

**¿Cómo calcula el total cada política?**

- `StandardPricing`: multiplica el precio por noche por el número de noches.
- `LongStayPricing`: aplica un descuento porcentual cuando la cantidad de noches alcanza o supera el mínimo configurado. En `Main`, el ejemplo se configura para aplicar un 10 % desde siete noches.

**¿Qué mecanismo de programación orientada a objetos permite intercambiar estas políticas?**

El polimorfismo. El servicio y la reserva reciben la interfaz `PricingPolicy`, por lo que pueden trabajar con cualquiera de sus implementaciones sin tener que conocer todos sus detalles internos.

**¿Se aplica automáticamente el descuento por estancia larga?**

En el menú actual, el usuario elige si desea utilizar la política de descuento. `LongStayPricing` calcula el importe con las condiciones configuradas, pero el menú es quien selecciona qué política se envía al crear la reserva.

**¿Por qué no incluir todos los cálculos directamente en `Reservation`?**

Separar las políticas evita que `Reservation` tenga que conocer todas las formas posibles de calcular una tarifa. Si se necesita otra política, se puede crear una implementación adicional de `PricingPolicy` sin reescribir la lógica principal de la reserva.

---

## Sobre las restricciones del negocio

**¿Cómo se evita registrar personas duplicadas?**

`HotelService` compara documento, correo y teléfono con los registros de clientes y trabajadores. Si encuentra una coincidencia, lanza `BusinessRuleException`.

**¿Cómo se evita registrar números de habitación repetidos?**

Antes de registrar una habitación, `HotelService.registerRoom(...)` busca si ya existe otra habitación con el mismo número. Si lo encuentra, rechaza la operación.

**¿Cómo se evita registrar usuarios repetidos para los trabajadores?**

`HotelService.registerEmployee(...)` revisa los usuarios de los trabajadores ya registrados y rechaza uno nuevo si coincide con otro, sin distinguir mayúsculas de minúsculas.

**¿Qué ocurre si se intenta reservar una habitación para fechas ocupadas?**

`HotelService.createReservation(...)` recorre las reservas existentes y utiliza `Reservation.occupies(...)`. Si una reserva activa corresponde a la misma habitación y se superpone con las fechas solicitadas, se lanza `BusinessRuleException` y no se guarda la nueva reserva.

**¿Dónde se validan los datos de entrada?**

`Validator` centraliza comprobaciones reutilizables, como campos obligatorios, longitudes máximas, identificadores y precios positivos, formato básico del correo y teléfono de diez dígitos. Las entidades llaman a esas validaciones al construirse o modificarse; `HotelService` aplica las reglas que requieren consultar varios registros.

**¿Cómo se distinguen los diferentes errores?**

Las excepciones `InvalidDataException`, `BusinessRuleException` y `ResourceNotFoundException` heredan de `HotelException`. De ese modo, el programa puede diferenciar datos inválidos, operaciones contrarias a las reglas y registros inexistentes.

---

## Sobre la organización en capas

**¿Qué clases pertenecen a cada paquete?**

| Paquete o ubicación | Clases y responsabilidad |
|---|---|
| Raíz | `Main`: crea los repositorios concretos, conecta las dependencias e inicia el menú. |
| `model` | `Person`, `Client`, `Employee`, `Room`, `Reservation`, `Invoice`, `Identifiable`, `PricingPolicy`, `StandardPricing` y `LongStayPricing`. Representan las entidades y contratos de comportamiento del dominio. |
| `persistence` | `Repository`, `InMemoryRepository`, `TextFileRepository`, `EntityFileMapper`, los mapeadores por entidad y `DelimitedText`. Define cómo consultar, guardar, actualizar y convertir registros. |
| `service` | `HotelService`. Coordina los casos de uso, consulta repositorios y aplica reglas que involucran varias entidades. |
| `ui` | `HotelApp`. Presenta el menú, recibe datos del usuario y muestra resultados. |
| `validation` | `Validator`. Reúne comprobaciones reutilizables para entradas y atributos. |
| `exceptions` | `HotelException`, `InvalidDataException`, `BusinessRuleException` y `ResourceNotFoundException`. Representan errores específicos de la aplicación. |
| `data/` | Archivos de texto en la raíz del proyecto: `clientes.txt`, `trabajadores.txt`, `habitaciones.txt`, `reservas.txt` y `comprobantes.txt`. |

**¿Qué criterio permite decidir dónde ubicar una clase?**

- Si representa una entidad o un comportamiento propio del dominio del hotel, se ubica en `model`.
- Si define o implementa operaciones para guardar y recuperar información, se ubica en `persistence`.
- Si coordina un caso de uso o una regla que involucra varias entidades o repositorios, se ubica en `service`.
- Si interactúa directamente con quien usa el programa, se ubica en `ui`.
- Si ofrece comprobaciones reutilizables, se ubica en `validation`.
- Si representa un error de la aplicación, se ubica en `exceptions`.

**¿Por qué el acceso a archivos no debe estar dentro de las entidades?**

Las clases de dominio deben representar la información y reglas propias de las personas, habitaciones, reservas y comprobantes. Si además leyeran o escribieran archivos, quedarían ligadas a detalles de almacenamiento. Separar esas responsabilidades permite cambiar la forma de persistencia con menos modificaciones en el modelo.

**¿Qué dependencias principales sigue el sistema?**

La dirección general de las operaciones es `ui → service → persistence`, y la persistencia utiliza los tipos del `model` para reconstruir o guardar entidades. `Main` funciona como punto de composición: crea la implementación concreta de los repositorios y se la entrega a `HotelService`.

`validation` y `exceptions` son paquetes auxiliares utilizados por las clases que necesitan validar datos o comunicar errores. En la implementación actual, algunas clases del modelo llaman a `Validator`; por eso estos paquetes auxiliares también aparecen como dependencias del modelo.

**¿Por qué `HotelApp` no debe guardar directamente en los archivos?**

El menú se encarga de la interacción con el usuario. Las operaciones de negocio deben pasar por `HotelService`, que decide cuándo consultar o modificar los repositorios. Esto evita repartir las reglas de almacenamiento por la interfaz.

---

## Sobre la persistencia en archivos de texto

**¿Cómo se conservan los datos entre ejecuciones?**

`Main` crea un `TextFileRepository` para cada tipo de entidad y le entrega la ruta del archivo y su mapeador. Al construirse, el repositorio intenta cargar los registros existentes. Cuando se guarda o actualiza una entidad, escribe el contenido actualizado en el archivo correspondiente.

**¿Qué formato utilizan los archivos?**

Cada línea representa un registro y sus campos se separan por punto y coma (`;`). `DelimitedText` centraliza la unión y separación de campos y maneja caracteres especiales para evitar que un punto y coma dentro de un valor sea interpretado como separador de columnas.

**¿Qué responsabilidad tienen los mapeadores?**

`ClientFileMapper`, `EmployeeFileMapper`, `RoomFileMapper`, `ReservationFileMapper` e `InvoiceFileMapper` convierten cada entidad en una línea y reconstruyen la entidad cuando se carga el archivo. Los mapeadores de reservas y comprobantes reciben repositorios relacionados para resolver las referencias por identificador.

**¿Para qué conservar `Repository` como interfaz?**

`Repository<T>` establece operaciones comunes, como `save`, `update`, `findById`, `findAll` y `nextId`. `InMemoryRepository` guarda los datos solo en memoria, mientras que `TextFileRepository` los conserva en archivos. `HotelService` trabaja con el contrato `Repository` y no necesita usar directamente una implementación específica.

**¿Qué limitaciones tiene esta persistencia?**

Es apropiada para una demostración académica sencilla, pero no ofrece las garantías transaccionales ni la concurrencia de una base de datos. Además, la contraseña del trabajador se guarda como texto plano en el archivo de datos; por ello, solo deben usarse datos ficticios y contraseñas de prueba.

---

## Sobre las decisiones de diseño (GRASP y SOLID)

**¿Cómo se aplica Controller (GRASP)?**

`HotelService` recibe las solicitudes que provienen de `HotelApp` y coordina operaciones como registrar personas, crear reservas, emitir comprobantes y registrar pagos. La interfaz no necesita acceder directamente a los repositorios.

**¿Cómo se aplica Information Expert (GRASP)?**

Las entidades mantienen las operaciones relacionadas con sus propios datos: `Reservation` calcula sus noches, comprueba si está activa, determina si ocupa una habitación en un periodo y controla su cancelación. `Invoice` controla la emisión, el pago y la anulación de su estado.

**¿Cómo se aplica Creator (GRASP)?**

`Reservation.create(...)` crea una reserva con los datos de cliente, habitación y fechas, y calcula su total utilizando una política de precio. `Invoice.issue(...)` construye el comprobante a partir de una reserva y su método de pago.

**¿Cómo se aplica Pure Fabrication (GRASP)?**

`HotelService`, `Validator` y los repositorios separan responsabilidades técnicas y de coordinación que no conviene concentrar en las entidades del dominio.

**¿Cómo se aplica responsabilidad única (SRP)?**

El menú gestiona la interacción, el servicio coordina los casos de uso, las entidades mantienen reglas propias, las políticas calculan tarifas y los repositorios manejan el almacenamiento. La separación no elimina todas las dependencias, pero ayuda a mantener responsabilidades distinguibles.

**¿Cómo se aplica abierto/cerrado (OCP)?**

`PricingPolicy` permite crear otra estrategia de tarifa implementando `calculateTotal`. La nueva estrategia puede seleccionarse desde `Main` y la interfaz sin tener que reescribir `Reservation`.

**¿Cómo se aplica inversión de dependencias (DIP)?**

`HotelService` recibe repositorios mediante el constructor y los declara mediante la interfaz `Repository`, en lugar de crear directamente objetos `TextFileRepository`. `Main` es quien selecciona las implementaciones concretas.

**¿Qué otros principios aparecen en el diseño?**

El polimorfismo está presente en `Person.describe()` y en las implementaciones de `PricingPolicy`. La interfaz `Repository` da un contrato común a las implementaciones de almacenamiento. Estas decisiones reducen el acoplamiento.

---

## Alcance actual y aspectos pendientes

- La aplicación es una demostración por consola; no tiene interfaz gráfica ni conexión activa a Oracle.
- Los archivos de texto guardan datos entre ejecuciones, pero no proporcionan las garantías de una base de datos transaccional.
- Las categorías `NEW`, `FREQUENT` y `VIP` se guardan para los clientes, pero actualmente no aplican beneficios automáticos.
- Los roles `ADMIN` y `RECEPTIONIST` se guardan para los trabajadores, pero el menú no aplica permisos diferentes por rol.
- Al crear una reserva, el servicio asocia al primer trabajador registrado si existe; el menú actual no permite seleccionar explícitamente al empleado para cada reserva.
- La política de descuento por estancia larga está disponible, y el menú permite elegir si se utiliza. El descuento no se selecciona automáticamente solo por la cantidad de noches.
