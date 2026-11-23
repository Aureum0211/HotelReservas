package persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Gestiona la conexion con Oracle. */
public final class Database {
    private Database() { }

    public static Connection open(Path configFile) {
        Properties config = load(configFile);
        String url = required(config, "db.url", configFile);
        String user = required(config, "db.user", configFile);
        String password = required(config, "db.password", configFile);
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException error) {
            throw new UncheckedIOException("No se pudo conectar a Oracle (" + url + "): "
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
}
