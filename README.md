# Hotel Reserva

Sistema de consola para administrar un hotel y sus habitaciones. Está desarrollado en Java y guarda la información en archivos de texto. La conexión con Oracle y la interfaz gráfica quedan para etapas posteriores.

## Requisitos

- JDK 17 o superior.
- Apache Maven.
- NetBeans 27 (opcional).

## Ejecutar

Abre el proyecto como proyecto Maven en NetBeans y ejecuta `Main.java`.

Desde una terminal situada en la raíz del proyecto:

```bash
mvn clean compile
java -cp target/classes Main
```

Ejecuta desde la raíz para que el programa encuentre la carpeta `data/`.

## Funcionalidades

- Inicio de sesión para trabajadores.
- Dos roles: recepcionista y administrador.
- Registro y consulta de clientes, trabajadores y habitaciones.
- Búsqueda de habitaciones disponibles por fechas y capacidad.
- Reservas, cancelaciones, check-in y check-out.
- Mantenimiento de habitaciones.
- Comprobantes con pagos completos o abonos parciales.
- Consulta del saldo pendiente.
- Reporte básico de ocupación e ingresos.
- Guardado de información en archivos TXT.

## Permisos

| Operación | Recepcionista | Administrador |
|---|:---:|:---:|
| Registrar clientes y gestionar reservas | Sí | Sí |
| Buscar habitaciones y hacer check-in/check-out | Sí | Sí |
| Emitir comprobantes y registrar abonos | Sí | Sí |
| Cancelar reservas sin pagos | Sí | Sí |
| Cancelar reservas con pagos | No | Sí |
| Registrar habitaciones y trabajadores | No | Sí |
| Consultar listados administrativos | No | Sí |
| Gestionar mantenimiento | No | Sí |
| Consultar reporte de ocupación e ingresos | No | Sí |

## Archivos TXT

La carpeta `data/` contiene clientes, trabajadores, habitaciones, reservas y comprobantes. No reemplaces esos archivos con datos de ejemplo sin hacer antes una copia. Los mapeadores admiten los formatos anteriores de habitaciones y comprobantes para conservar la información existente.

- `trabajadores.txt`: un rol antiguo `MANAGER` se interpreta como `ADMIN`.
- `habitaciones.txt`: se añade un indicador de mantenimiento al final; los registros antiguos se leen como fuera de mantenimiento.
- `comprobantes.txt`: se añade el total abonado al final; los registros antiguos se cargan con base en su estado anterior.

## Organización del código

```text
src/main/java/
├── Main.java
├── model/        Entidades y reglas del hotel
├── persistence/  Lectura y escritura de archivos TXT
├── service/      Operaciones del sistema
├── ui/           Menú de consola
├── validation/   Validaciones
└── exceptions/   Excepciones
```

## Consideraciones

- El sistema contempla únicamente hoteles y habitaciones.
- Las cancelaciones no generan reembolsos automáticos. Los comprobantes pagados quedan como registro y los pendientes se anulan.
- Si se cancela una reserva con un abono parcial, el comprobante se anula, conserva el valor ya recibido y cierra el saldo restante.
- Las contraseñas de demostración se guardan en texto plano en `trabajadores.txt`. No debe usarse así en un sistema real; al incorporar Oracle conviene almacenar hashes de contraseñas.
- Los reportes son básicos y utilizan los datos actuales del sistema; no sustituyen un informe contable.
