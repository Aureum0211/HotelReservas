package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import model.Identifiable;

/** Convierte una fila de Oracle en un objeto Java. */
public interface RowMapper<T extends Identifiable> {
    String table();
    T read(ResultSet rs) throws SQLException;
}
