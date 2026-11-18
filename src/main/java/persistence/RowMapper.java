package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.Identifiable;

/**
 * Describe como se guarda una entidad en una tabla de la base de datos y como se
 * reconstruye a partir de una fila.
 * @param <T> tipo de entidad que maneja el convertidor.
 */
public interface RowMapper<T extends Identifiable> {

    /**
     * Devuelve el nombre de la tabla donde se guarda la entidad.
     * @return nombre de la tabla.
     */
    String table();

    /**
     * Devuelve los nombres de las columnas de la tabla, sin incluir la columna id.
     * @return nombres de columnas en el mismo orden que {@link #values(Identifiable)}.
     */
    List<String> columns();

    /**
     * Convierte la entidad en los valores que se guardaran en las columnas. Los valores
     * pueden ser numeros, texto o nulos; las fechas y enumeraciones se guardan como texto.
     * @param entity entidad que se va a guardar.
     * @return valores en el mismo orden que {@link #columns()}.
     */
    Object[] values(T entity);

    /**
     * Reconstruye la entidad a partir de la fila actual del resultado.
     * @param rs resultado posicionado en la fila que se va a leer.
     * @return entidad reconstruida.
     * @throws SQLException si no se puede leer alguna columna.
     */
    T read(ResultSet rs) throws SQLException;
}
