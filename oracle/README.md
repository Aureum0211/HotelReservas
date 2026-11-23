# Instalación y configuración de Oracle — HotelReservas

Esta carpeta contiene los scripts que crean y preparan el esquema de HotelReservas dentro de una instancia Oracle Database existente.

> **Importante:** los scripts no instalan Oracle Database ni crean el servidor. Crean las tablas, funciones, triggers, paquetes, vistas, índices y datos iniciales del proyecto dentro de un usuario/esquema Oracle.

## Requisitos

- Oracle Database con soporte PL/SQL (el proyecto se trabajó con Oracle Database XE 21c).
- Un usuario/esquema donde crear los objetos de HotelReservas.
- SQL Developer u otra herramienta que permita ejecutar scripts SQL y PL/SQL.
- Para ejecutar la aplicación: JDK 17 o superior y Maven 3.8 o superior.

## 1. Crear el usuario/esquema

Si ya tienes un usuario para HotelReservas, puedes reutilizarlo y pasar al apartado 2. Si vas a crear uno nuevo, conéctate desde SQL Developer como `SYSTEM` (o una cuenta DBA) **a la PDB/servicio correcto**, y ejecuta un ejemplo como este:

```sql
CREATE USER HOTEL IDENTIFIED BY "Cambia_Esta_Clave"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE,
      CREATE PROCEDURE, CREATE TRIGGER
TO HOTEL;
```

- Cambia la contraseña de ejemplo por una propia.
- Confirma el nombre real de tu servicio Oracle. En Oracle XE puede ser `XEPDB1` o `xepdb1`, pero no debes asumirlo: compruébalo en la conexión de SQL Developer o en el listener.
- Si el usuario `HOTEL` ya existe, no repitas `CREATE USER`; utiliza el esquema existente y comprueba que tenga los permisos necesarios.
- Los roles `ROL_RECEPCIONISTA` y `ROL_ADMINISTRADOR` que intenta crear el script `09_mejoras_bd.sql` son complementarios. Si el esquema no tiene `CREATE ROLE`, ese paso puede mostrar un aviso; la aplicación académica no depende de esos roles de Oracle.

Después, crea una conexión en SQL Developer con el usuario del esquema (`HOTEL`) y la PDB/servicio que elegiste. Ejecuta los scripts siguientes conectado como ese usuario, no como `SYS` ni `SYSTEM`.

## 2. Ejecutar los scripts

Abre cada archivo en SQL Developer y utiliza **Run Script (F5)**. Para una instalación nueva, ejecútalos en este orden:

| Orden | Archivo | Contenido |
|---:|---|---|
| 1 | `01_crear_tablas.sql` | Crea `CLIENTES`, `TRABAJADORES`, `HABITACIONES`, `RESERVAS` y `COMPROBANTES`. |
| 2 | `02_funciones.sql` | Crea `FN_NOCHES` y `FN_HAY_SOLAPE`. |
| 3 | `03_triggers.sql` | Crea `AUDITORIA_RESERVAS`, su secuencia y triggers de validación/auditoría. |
| 4 | `04_base_comun.sql` | Crea secuencias, funciones y validación de administradores; agrega trabajadores de demostración si `TRABAJADORES` está vacía. |
| 5 | `05_paquete_reservas.sql` | Crea `PKG_RESERVAS`: reservas, disponibilidad, estados, comprobantes y pagos. |
| 6 | `06_paquete_personas.sql` | Crea `PKG_PERSONAS`: clientes, huéspedes, trabajadores e inicio de sesión. |
| 7 | `07_paquete_habitaciones.sql` | Crea `PKG_HABITACIONES`: registro y mantenimiento. |
| 8 | `08_paquete_reportes.sql` | Crea `PKG_REPORTES`: estadísticas, reportes y rankings. |
| 9 | `09_mejoras_bd.sql` | Agrega restricciones, índices, vistas y roles complementarios. |
| 10 | `10_pruebas_paquetes.sql` | Ejecuta pruebas integradas de los paquetes. El resultado esperado es cero fallos. |

Los scripts de creación usan `CREATE` o `CREATE OR REPLACE` y algunos ignoran objetos que ya existen. Aun así, no son una herramienta general de migración para cualquier esquema: antes de ejecutarlos en una base con datos, realiza un respaldo y confirma que las tablas existentes tengan la estructura esperada.

### Si ya tienes las tablas creadas

No borres las tablas ni los datos. Si tu esquema corresponde a la versión de HotelReservas, puedes ejecutar los scripts de funciones, triggers y paquetes para instalar o actualizar la lógica. Revisa especialmente las salidas de `USER_ERRORS` y el estado de los objetos. No ejecutes scripts históricos de recreación o eliminación de tablas.

## 3. Datos iniciales y cuentas de demostración

`04_base_comun.sql` crea las secuencias y funciones comunes. Inserta cuatro trabajadores de demostración y hace `COMMIT` **solo cuando `TRABAJADORES` está vacía**:

| Usuario de la aplicación | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `recepcion1` | `recep123` | `RECEPTIONIST` |
| `recepcion2` | `recep456` | `RECEPTIONIST` |
| `inactivo` | `inactivo123` | `RECEPTIONIST` (inactivo) |

Estas son credenciales de inicio de sesión de HotelReservas. No son el usuario y la contraseña Oracle que se escriben en `db.properties`.

## 4. Ejecutar las pruebas y comprobar los paquetes

Antes de ejecutar `10_pruebas_paquetes.sql`, confirma que los identificadores de prueba desde `900001` y los documentos, correos y teléfonos utilizados en el script no existan ya en tus tablas. Ejecuta el archivo con F5.

El bloque de prueba utiliza datos temporales y termina con `ROLLBACK`; las filas de prueba se revierten. Las secuencias Oracle pueden avanzar aunque se haga `ROLLBACK`, por lo que pueden quedar saltos en los identificadores.

Después comprueba el estado de los paquetes:

```sql
SELECT object_name, object_type, status
FROM user_objects
WHERE object_type IN ('PACKAGE', 'PACKAGE BODY')
  AND object_name IN (
      'PKG_RESERVAS', 'PKG_PERSONAS', 'PKG_HABITACIONES', 'PKG_REPORTES'
  )
ORDER BY object_name, object_type;

SELECT name, type, line, text
FROM user_errors
WHERE name IN (
    'PKG_RESERVAS', 'PKG_PERSONAS', 'PKG_HABITACIONES', 'PKG_REPORTES'
)
ORDER BY name, type, sequence;
```

Los cuatro paquetes y sus cuerpos deben estar `VALID`; la segunda consulta no debe devolver filas. La batería de pruebas debe terminar con `0 fallan`.

## 5. Configurar la aplicación Java

En la carpeta raíz de HotelReservas (donde está `pom.xml`), copia `db.properties.example` como `db.properties` y completa los valores:

```properties
db.url=jdbc:oracle:thin:@//HOST:1521/SERVICE_NAME
db.user=HOTEL
db.password=TU_CLAVE_DEL_ESQUEMA
```

En Windows PowerShell:

```powershell
Copy-Item db.properties.example db.properties
```

En CMD:

```cmd
copy db.properties.example db.properties
```

Ejemplo cuando Oracle está instalado en el mismo equipo que ejecuta la aplicación:

```properties
db.url=jdbc:oracle:thin:@//localhost:1521/xepdb1
db.user=HOTEL
db.password=TU_CLAVE_DEL_ESQUEMA
```

Usa el host, puerto y service name reales de tu instancia. El nombre `xepdb1` es un ejemplo, no una constante del proyecto. No uses `SYS` ni `SYSTEM` como cuenta JDBC de la aplicación.

No publiques `db.properties`: contiene credenciales de conexión. El repositorio debe incluir solamente `db.properties.example`.

## 6. Conectar varias computadoras a una misma base de datos

HotelReservas utiliza JDBC para conectarse directamente a Oracle. Varias computadoras pueden compartir la misma información si todas se conectan a **la misma instancia, servicio y esquema Oracle**. Solo una computadora (o servidor) aloja Oracle; los demás equipos ejecutan la aplicación como clientes.

### En el equipo servidor (donde está Oracle)

1. Instala y configura Oracle Database y ejecuta los scripts de esta carpeta una sola vez en el esquema del proyecto.
2. Conecta el servidor a la red local por Wi-Fi o Ethernet.
3. En Windows, abre CMD y ejecuta `ipconfig`. Anota la IPv4 privada de la interfaz que comparte la red con los clientes (por ejemplo, `192.168.1.50`). Si es posible, configura una reserva DHCP en el router para que esa IP no cambie.
4. Comprueba el listener con:

   ```cmd
   lsnrctl status
   ```

   Verifica que anuncie el puerto TCP configurado (habitualmente `1521`) y el service name de la PDB utilizada. Para comprobar el puerto en Windows también puedes ejecutar `netstat -ano | findstr :1521`.

5. Crea una regla de entrada en Windows Firewall para TCP y el puerto del listener (normalmente `1521`). Limita el perfil a **Privado** y, si es posible, el equipo remoto a las IP privadas autorizadas.
6. Mantén Oracle, el listener, el equipo servidor y la conexión de red activos mientras los clientes utilicen el sistema.

El listener debe escuchar en una interfaz alcanzable desde la red y no solo en `localhost`/`127.0.0.1`. No modifiques `listener.ora` si `lsnrctl status` ya muestra un endpoint de red válido.

### En cada equipo cliente

1. Descarga o copia el proyecto. Instala JDK 17 o superior y Maven 3.8 o superior si ejecutará la aplicación desde el código fuente.
2. **No necesita instalar Oracle Database ni SQL Developer**, y no debe ejecutar los scripts SQL.
3. Copia `db.properties.example` como `db.properties` en la raíz del proyecto.
4. En `db.url`, cambia `localhost` por la IPv4 privada o nombre de red del servidor. Conserva el puerto y service name correctos. Ejemplo:

   ```properties
   db.url=jdbc:oracle:thin:@//192.168.1.50:1521/xepdb1
   db.user=HOTEL
   db.password=LA_CLAVE_DEL_ESQUEMA
   ```

5. Antes de abrir la aplicación, comprueba desde PowerShell del cliente que el puerto sea alcanzable:

   ```powershell
   Test-NetConnection -ComputerName 192.168.1.50 -Port 1521
   ```

   Debe aparecer `TcpTestSucceeded : True`. Esto comprueba la conectividad TCP; la autenticación Oracle se confirma al abrir la aplicación.

6. Ejecuta desde la raíz del proyecto:

   ```bash
   mvn clean javafx:run
   ```

Todos los clientes deben apuntar al mismo host, puerto, service name y esquema. Una reserva registrada en un equipo aparecerá en los demás porque se guarda en la misma base de datos. Cada equipo puede iniciar sesión en HotelReservas con su usuario de trabajador; las credenciales Oracle de `db.properties` son las que usa la aplicación para conectar con el esquema.

### Si los equipos están en redes distintas

No publiques el puerto `1521` directamente en Internet. Para una conexión remota, utiliza una VPN entre los equipos o una red privada administrada. En un despliegue comercial sería preferible interponer un backend/API entre las aplicaciones cliente y Oracle, en vez de distribuir credenciales del esquema Oracle a cada computadora.

## 7. Iniciar HotelReservas

Desde la carpeta que contiene `pom.xml` y `db.properties`:

```bash
mvn clean javafx:run
```

NetBeans puede abrir el proyecto como Maven y ejecutar el objetivo `javafx:run`. SQL Developer solo es necesario para administrar el esquema; no se necesita en cada equipo cliente.
