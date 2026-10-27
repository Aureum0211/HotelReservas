package repository;

import java.util.List;
import java.util.Optional;
import model.Identifiable;

public interface Repository<T extends Identifiable> {

    void save(T entity);

    Optional<T> findById(int id);

    List<T> findAll();

    int nextId();
}
