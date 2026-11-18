package persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Abre la conexion a la base de datos Oracle usando los datos de un archivo de
 * propiedades, crea las tablas que falten y carga los trabajadores de prueba cuando la
 * tabla esta vacia.
 */
public final class Database {

    /** Codigo de error de Oracle cuando la tabla ya existe (ORA-00955). */
    private static final int NAME_ALREADY_USED = 955;

    private Database() { }

    /**
     * Abre la conexion y deja la base lista para usar.
     * @param configFile archivo de propiedades con db.url, db.user y db.password.
     * @return conexion lista para usar.
     * @throws UncheckedIOException si falta el archivo, la conexion falla o no se pueden
     *         crear las tablas.
     */
    public static Connection open(Path configFile) {
        Properties config = load(configFile);
        String url = required(config, "db.url", configFile);
        String user = required(config, "db.user", configFile);
        String password = required(config, "db.password", configFile);
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            try (Statement statement = connection.createStatement()) {
                createSchema(statement);
            }
            seedEmployees(connection);
            return connection;
        } catch (SQLException error) {
            throw new UncheckedIOException("No se pudo conectar o preparar la base de datos (" + url + "): "
                    + error.getMessage(), new IOException(error));
        }
    }

    private static Properties load(Path configFile) {
        if (!Files.exists(configFile)) {
            throw new UncheckedIOException("No se encontro el archivo de conexion " + configFile.toAbsolutePath()
                    + ". Debe estar en la carpeta desde la que ejecutas el programa.",
                    new IOException("archivo inexistente"));
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configFile)) {
            properties.load(in);
        } catch (IOException error) {
            throw new UncheckedIOException("No se pudo leer " + configFile + ": " + error.getMessage(), error);
        }
        return properties;
    }

    private static String required(Properties properties, String key, Path file) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta '" + key + "' en " + file + ".");
        }
        return value.trim();
    }

    /** Crea una tabla; si ya existe (ORA-00955) no hace nada. */
    private static void createTable(Statement statement, String sql) throws SQLException {
        try {
            statement.execute(sql);
        } catch (SQLException error) {
            if (error.getErrorCode() != NAME_ALREADY_USED) throw error;
        }
    }

    private static void createSchema(Statement s) throws SQLException {
        createTable(s, """
                CREATE TABLE clientes (
                    id NUMBER(10) PRIMARY KEY,
                    nombre VARCHAR2(100) NOT NULL,
                    documento VARCHAR2(30) NOT NULL,
                    correo VARCHAR2(100) NOT NULL,
                    telefono VARCHAR2(20) NOT NULL,
                    ciudad VARCHAR2(60) NOT NULL,
                    categoria VARCHAR2(10) NOT NULL CHECK (categoria IN ('NEW', 'FREQUENT', 'VIP')),
                    activo NUMBER(1) DEFAULT 1 NOT NULL CHECK (activo IN (0, 1))
                )""");
        createTable(s, """
                CREATE TABLE trabajadores (
                    id NUMBER(10) PRIMARY KEY,
                    nombre VARCHAR2(100) NOT NULL,
                    documento VARCHAR2(30) NOT NULL,
                    correo VARCHAR2(100) NOT NULL,
                    telefono VARCHAR2(20) NOT NULL,
                    ciudad VARCHAR2(60) NOT NULL,
                    activo NUMBER(1) DEFAULT 1 NOT NULL CHECK (activo IN (0, 1)),
                    rol VARCHAR2(15) NOT NULL CHECK (rol IN ('RECEPTIONIST', 'ADMIN')),
                    usuario VARCHAR2(30) NOT NULL UNIQUE,
                    contrasena VARCHAR2(50) NOT NULL
                )""");
        createTable(s, """
                CREATE TABLE habitaciones (
                    id NUMBER(10) PRIMARY KEY,
                    numero VARCHAR2(20) NOT NULL UNIQUE,
                    tipo VARCHAR2(10) NOT NULL CHECK (tipo IN ('SINGLE', 'DOUBLE', 'SUITE')),
                    capacidad NUMBER(5) NOT NULL CHECK (capacidad > 0),
                    precio_noche NUMBER NOT NULL CHECK (precio_noche > 0),
                    disponible NUMBER(1) DEFAULT 1 NOT NULL CHECK (disponible IN (0, 1)),
                    en_mantenimiento NUMBER(1) DEFAULT 0 NOT NULL CHECK (en_mantenimiento IN (0, 1))
                )""");
        createTable(s, """
                CREATE TABLE reservas (
                    id NUMBER(10) PRIMARY KEY,
                    cliente_id NUMBER(10) NOT NULL REFERENCES clientes(id),
                    trabajador_id NUMBER(10) REFERENCES trabajadores(id),
                    habitacion_id NUMBER(10) NOT NULL REFERENCES habitaciones(id),
                    entrada VARCHAR2(10) NOT NULL,
                    salida VARCHAR2(10) NOT NULL,
                    precio_noche NUMBER NOT NULL,
                    total NUMBER NOT NULL,
                    huespedes NUMBER(5) NOT NULL CHECK (huespedes > 0),
                    estado VARCHAR2(15) NOT NULL CHECK (estado IN ('ACTIVE', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'))
                )""");
        createTable(s, """
                CREATE TABLE comprobantes (
                    id NUMBER(10) PRIMARY KEY,
                    reserva_id NUMBER(10) NOT NULL REFERENCES reservas(id),
                    fecha_emision VARCHAR2(10) NOT NULL,
                    monto NUMBER NOT NULL,
                    metodo_pago VARCHAR2(10) NOT NULL CHECK (metodo_pago IN ('CASH', 'CARD', 'TRANSFER')),
                    estado VARCHAR2(15) NOT NULL CHECK (estado IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'REFUNDED', 'VOIDED')),
                    reembolso NUMBER DEFAULT 0 NOT NULL,
                    pagado NUMBER DEFAULT 0 NOT NULL
                )""");
    }

    /** Inserta los usuarios de prueba solo si todavia no hay ningun trabajador. */
    private static void seedEmployees(Connection connection) throws SQLException {
        try (Statement count = connection.createStatement();
             var rs = count.executeQuery("SELECT COUNT(*) FROM trabajadores")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        Object[][] employees = {
            {1, "Administrador", "1000000001", "admin@hotel.com", "3001234567", "Valledupar", 1, "ADMIN", "admin", "admin123"},
            {2, "Laura Gomez", "1000000002", "laura@hotel.com", "3002345678", "Valledupar", 1, "RECEPTIONIST", "recepcion1", "recep123"},
            {3, "Carlos Perez", "1000000003", "carlos@hotel.com", "3003456789", "Valledupar", 1, "RECEPTIONIST", "recepcion2", "recep456"},
            {4, "Ana Torres", "1000000004", "ana@hotel.com", "3004567890", "Valledupar", 0, "RECEPTIONIST", "inactivo", "inactivo123"}
        };
        String sql = "INSERT INTO trabajadores (id, nombre, documento, correo, telefono, ciudad, activo, rol, usuario, contrasena)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] employee : employees) {
                for (int i = 0; i < employee.length; i++) statement.setObject(i + 1, employee[i]);
                statement.executeUpdate();
            }
        }
    }
}
