# Estado de la refactorización y comprobaciones

## Cambios incluidos

- `HotelService` funciona como fachada y delega las escrituras, validaciones de negocio, cálculos y reportes en Oracle.
- `OracleHotelProcedures` centraliza las llamadas JDBC a `PKG_RESERVAS`, `PKG_PERSONAS`, `PKG_HABITACIONES` y `PKG_REPORTES`.
- Se agregó `model.ReportRows` para representar las filas devueltas por los cursores de reportes.
- `Reportes` y `TopHabitacionesController` consumen resultados ya calculados en Oracle.
- `Database` solo abre la conexión. La instalación del esquema y la carga inicial de trabajadores se trasladaron a scripts SQL.
- Los repositorios Java son de lectura; las escrituras de negocio se realizan mediante PL/SQL.
- Se retiró el almacenamiento TXT antiguo y la antigua interfaz de consola.
- Se conservaron los FXML, imágenes y estilos de JavaFX.
- Se quitó la validación duplicada del rango de fechas de reportes en Java para que Oracle aplique la regla.

## Correcciones SQL incorporadas

- `oracle/09_paquete_reservas.sql`: normaliza las fechas de entrada y salida antes de insertarlas (`AAAA-MM-DD`).
- `oracle/14_paquete_habitaciones.sql`: rechaza precios que, al redondearse a dos decimales, quedan en cero.

Estas correcciones deben confirmarse ejecutando `16_pruebas_paquetes.sql` en el esquema Oracle del proyecto.

## Comprobaciones realizadas en este entorno

- Compilación con `javac --release 17` de modelos, excepciones, validación, persistencia, `HotelService` y el adaptador de reportes. Para esta verificación se utilizó un stub temporal de `oracle.jdbc.OracleTypes` y un stub de `Ui` para compilar `Reportes`; esos stubs **no** están incluidos en el proyecto entregable.
- XML de `pom.xml` y archivos FXML bien formados.
- Los `fx:id` encontrados en los FXML existen como campos en sus controladores.
- Los manejadores declarados en los FXML tienen métodos correspondientes en sus controladores.

## Comprobaciones que faltan en el equipo del proyecto

Este entorno no dispone de Maven, las bibliotecas JavaFX ni una instancia Oracle; por eso no se pudo ejecutar una compilación Maven completa, abrir la interfaz real ni ejecutar los scripts contra Oracle.

Antes de la sustentación, en el equipo donde se desarrolló el proyecto:

1. Configura `db.properties` en la raíz usando `db.properties.example` como plantilla.
2. Instala/actualiza Oracle siguiendo `oracle/LEEME.md`.
3. Comprueba que los cuatro paquetes y sus cuerpos estén `VALID`.
4. Ejecuta `16_pruebas_paquetes.sql` y verifica el resultado `0 fallan`.
5. Abre el proyecto como Maven en NetBeans o ejecuta `mvn clean javafx:run`.
6. Prueba al menos login, consulta de habitaciones, creación/cancelación de reserva, comprobante/pago, mantenimiento, reportes y ranking.

No ejecutes los scripts históricos `06_recrear_tablas.sql` ni `10_pruebas_paquete.sql` sobre el esquema actual. El segundo fue reemplazado por `16_pruebas_paquetes.sql`.
