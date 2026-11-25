# HotelReservas

Sistema de gestión hotelera desarrollado para facilitar la administración de habitaciones, clientes, reservas y comprobantes. Permite gestionar las operaciones principales de un hotel mediante una interfaz gráfica y almacenar la información en una base de datos Oracle.

## Tecnologías

- **Java 17:** lenguaje de programación.
- **JavaFX:** desarrollo de la interfaz gráfica.
- **Oracle Database:** almacenamiento de la información.
- **PL/SQL:** procedimientos, funciones y validaciones de la base de datos.
- **JDBC:** conexión entre Java y Oracle.
- **Maven:** administración de dependencias y ejecución del proyecto.

## Arquitectura

El proyecto utiliza una arquitectura por capas que separa la interfaz gráfica, la lógica de aplicación, el acceso a datos y los modelos.

- **UI:** pantallas y controladores JavaFX.
- **Service:** coordinación de las operaciones del sistema.
- **Persistence:** conexión y comunicación con Oracle.
- **Model:** clases que representan los datos del sistema.

## Base de datos

El sistema utiliza Oracle Database para almacenar la información de clientes, trabajadores, habitaciones, reservas y comprobantes.

La lógica de las operaciones se complementa con paquetes, funciones y triggers desarrollados en PL/SQL.

El esquema se encuentra en el siguiente archivo:

[`HotelReservas_Esquema_Simplificado.sql`](./oracle/HotelReservas_Esquema_Simplificado.sql)

Para preparar la base de datos:

1. Crear y configurar el esquema Oracle `HOTEL`.
2. Abrir el archivo SQL en SQL Developer y ejecutarlo conectado con el usuario `HOTEL`.
3. Configurar los datos de conexión en `db.properties`.

## Requisitos

- JDK 17 o superior.
- Apache Maven.
- Oracle Database 21c XE o compatible.
- Esquema Oracle configurado para el proyecto.

## Configuración y ejecución

1. Clonar o descargar el repositorio.
2. Configurar `db.properties` con los datos de conexión a Oracle.
3. Abrir una terminal en la carpeta del proyecto.
4. Ejecutar:

```bash
mvn clean javafx:run
```

## Funcionalidades

- Inicio de sesión para trabajadores y acceso de huéspedes.
- Registro y consulta de clientes.
- Administración de habitaciones.
- Creación, consulta y cancelación de reservas.
- Gestión de comprobantes y pagos.
- Control de mantenimiento de habitaciones.
- Generación de reportes administrativos.
- Exportación de reportes en formato CSV.