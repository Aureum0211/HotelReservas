# Hotel Reservas

Sistema de información para la gestión de reservas hoteleras. Permite registrar y consultar clientes, trabajadores, habitaciones, reservas y comprobantes, con persistencia de datos en archivos de texto.

## Arquitectura

El proyecto está organizado en capas dentro de `src/main/java`:

```text
HotelReservas/
├── pom.xml
├── data/
│   ├── clientes.txt
│   ├── trabajadores.txt
│   ├── habitaciones.txt
│   ├── reservas.txt
│   └── comprobantes.txt
├── docs/
│   ├── analisis.md
│   ├── class-diagram.md
│   ├── hierarchy-diagram.md
│   ├── layers-diagram.md
├── README.md
├── GUIA_COMMITS.md
├── .gitignore
└── src/
    └── main/
        └── java/
            ├── Main.java
            ├── model/
            │   ├── Person.java
            │   ├── Client.java
            │   ├── Employee.java
            │   ├── Room.java
            │   ├── Reservation.java
            │   ├── Invoice.java
            │   ├── Identifiable.java
            │   ├── PricingPolicy.java
            │   ├── DiscountPricing.java
            │   ├── StandardPricing.java
            │   └── LongStayPricing.java
            ├── persistence/
            │   ├── Repository.java
            │   ├── InMemoryRepository.java
            │   ├── TextFileRepository.java
            │   ├── EntityFileMapper.java
            │   ├── ClientFileMapper.java
            │   ├── EmployeeFileMapper.java
            │   ├── RoomFileMapper.java
            │   ├── ReservationFileMapper.java
            │   ├── InvoiceFileMapper.java
            │   └── DelimitedText.java
            ├── service/
            │   └── HotelService.java
            ├── ui/
            │   └── HotelApp.java
            ├── validation/
            │   └── Validator.java
            └── exceptions/
                ├── HotelException.java
                ├── BusinessRuleException.java
                ├── InvalidDataException.java
                └── ResourceNotFoundException.java
```

Dependencias principales: `ui → service → persistence → model`. `Main.java` crea las implementaciones concretas e inicia la aplicación.

## Requisitos

- JDK 17
- Maven

## Compilación y ejecución

**NetBeans:** abrir el proyecto mediante **File → Open Project** y ejecutar `Main.java`.

**Terminal:** desde la raíz del proyecto, ejecutar:

```bash
mvn compile
java -cp target/classes Main
```

Ejecutar desde la raíz permite que la aplicación encuentre la carpeta `data/`.

## Funcionalidades

- Registrar y consultar clientes y trabajadores.
- Registrar y consultar habitaciones.
- Buscar habitaciones disponibles según fechas y cantidad de personas.
- Crear, consultar y cancelar reservas.
- Validar los datos, la capacidad de la habitación, las fechas y evitar reservas superpuestas.
- Aplicar descuentos automáticos: 10 % desde 7 noches, más 5 % para clientes frecuentes o 15 % para VIP.
- Emitir comprobantes (uno por reserva) y registrar pagos. Al cancelar una reserva se anula su comprobante pendiente.
- Cargar y guardar los datos al iniciar la aplicación y al realizar cambios.

## Documentación adicional

- [`docs/analisis.md`](docs/analisis.md): decisiones de diseño del sistema.
- [`docs/class-diagram.md`](docs/class-diagram.md): diagrama de clases.
- [`docs/hierarchy-diagram.md`](docs/hierarchy-diagram.md): jerarquías de herencia e interfaces.
- [`docs/layers-diagram.md`](docs/layers-diagram.md): diagrama de capas y dependencias.
