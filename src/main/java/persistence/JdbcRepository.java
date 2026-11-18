package persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Identifiable;

/**
 * Repositorio que guarda las entidades en una tabla de una base de datos relacional
 * mediante JDBC. Los identificadores los sigue asignando la aplicacion con
 * {@link #nextId()}.
 * @param <T> tipo de entidad almacenada.
 */
public class JdbcRepository<T extends Identifiable> implements Repository<T> {
    private final Connection connection;
    private final RowMapper<T> mapper;

    /**
     * Crea un repositorio sobre una conexion ya abierta.
     * @param connection conexion a la base de datos.
     * @param mapper convertidor entre la entidad y las filas de su tabla.
     */
    public JdbcRepository(Connection connection, RowMapper<T> mapper) {
        this.connection = connection;
        this.mapper = mapper;
    }

    /**
     * Inserta una entidad nueva en la tabla.
     * @param entity entidad que se desea almacenar.
     * @throws IllegalArgumentException si la entidad es nula o su identificador ya existe.
     * @throws UncheckedIOException si la base de datos rechaza la operacion.
     */
    @Override
    public void save(T entity) {
        if (entity == null) throw new IllegalArgumentException("No se puede guardar un objeto nulo.");
        if (findById(entity.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un registro con ID " + entity.getId() + ".");
        }
        List<String> columns = mapper.columns();
        String sql = "INSERT INTO " + mapper.table() + " (id, " + String.join(", ", columns) + ") VALUES (?"
                + ", ?".repeat(columns.size()) + ")";
        Object[] values = mapper.values(entity);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entity.getId());
            for (int i = 0; i < values.length; i++) bind(statement, i + 2, values[i]);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw fail("guardar en " + mapper.table(), error);
        }
    }

    /**
     * Actualiza una entidad existente.
     * @param entity entidad con los datos nuevos.
     * @throws IllegalArgumentException si la entidad es nula o no existe en la tabla.
     * @throws UncheckedIOException si la base de datos rechaza la operacion.
     */
    @Override
    public void update(T entity) {
        if (entity == null) throw new IllegalArgumentException("No se puede actualizar un objeto nulo.");
        List<String> columns = mapper.columns();
        List<String> assignments = new ArrayList<>();
        for (String column : columns) assignments.add(column + " = ?");
        String sql = "UPDATE " + mapper.table() + " SET " + String.join(", ", assignments) + " WHERE id = ?";
        Object[] values = mapper.values(entity);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) bind(statement, i + 1, values[i]);
            statement.setInt(values.length + 1, entity.getId());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe un registro con ID " + entity.getId() + " para actualizar.");
            }
        } catch (SQLException error) {
            throw fail("actualizar en " + mapper.table(), error);
        }
    }

    /**
     * Busca una entidad por su identificador.
     * @param id identificador buscado.
     * @return la entidad, o vacio si no existe.
     */
    @Override
    public Optional<T> findById(int id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + mapper.table() + " WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapper.read(rs)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw fail("leer de " + mapper.table(), error);
        }
    }

    /**
     * Devuelve todas las entidades de la tabla, ordenadas por identificador.
     * @return lista de entidades almacenadas.
     */
    @Override
    public List<T> findAll() {
        List<T> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + mapper.table() + " ORDER BY id");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.add(mapper.read(rs));
            return result;
        } catch (SQLException error) {
            throw fail("leer de " + mapper.table(), error);
        }
    }

    /**
     * Calcula el siguiente identificador libre de la tabla.
     * @return el mayor identificador existente mas uno.
     */
    @Override
    public int nextId() {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM " + mapper.table());
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 1;
        } catch (SQLException error) {
            throw fail("calcular el siguiente id de " + mapper.table(), error);
        }
    }

    /** Asigna un valor al parametro; los nulos se envian con setNull para que Oracle los acepte. */
    private static void bind(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value == null) statement.setNull(index, Types.NULL);
        else statement.setObject(index, value);
    }

    private UncheckedIOException fail(String action, SQLException error) {
        return new UncheckedIOException("No se pudo " + action + ": " + error.getMessage(), new IOException(error));
    }
}
