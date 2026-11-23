package persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Identifiable;

/** Repositorio JDBC para consultar datos. */
public class JdbcRepository<T extends Identifiable> implements Repository<T> {
    private final Connection connection;
    private final RowMapper<T> mapper;

    public JdbcRepository(Connection connection, RowMapper<T> mapper) {
        this.connection = connection;
        this.mapper = mapper;
    }

    @Override
    public Optional<T> findById(int id) {
        String sql = "SELECT * FROM " + mapper.table() + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapper.read(rs)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw fail("leer de " + mapper.table(), error);
        }
    }

    @Override
    public List<T> findAll() {
        List<T> result = new ArrayList<>();
        String sql = "SELECT * FROM " + mapper.table() + " ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.add(mapper.read(rs));
            return result;
        } catch (SQLException error) {
            throw fail("leer de " + mapper.table(), error);
        }
    }

    private UncheckedIOException fail(String action, SQLException error) {
        return new UncheckedIOException("No se pudo " + action + ": " + error.getMessage(), new IOException(error));
    }
}
