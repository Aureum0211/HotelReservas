package persistence;

import java.util.List;
import java.util.Optional;
import model.Identifiable;

/** Define las consultas disponibles para las entidades. */
public interface Repository<T extends Identifiable> {
    Optional<T> findById(int id);
    List<T> findAll();
}
