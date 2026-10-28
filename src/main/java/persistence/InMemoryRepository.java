package persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Identifiable;

public class InMemoryRepository<T extends Identifiable> implements Repository<T> {

    private final List<T> entities = new ArrayList<>();

    @Override
    public void save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("No se puede guardar esta vacio .");
        }
        if (findById(entity.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un registro con ID " + entity.getId() + ".");
        }
        entities.add(entity);
    }

    @Override
    public void update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("No se puede actualizar un objeto nulo.");
        }
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId() == entity.getId()) {
                entities.set(i, entity);
                return;
            }
        }
        throw new IllegalArgumentException("No existe un registro con ID " + entity.getId() + " para actualizar.");
    }

    @Override
    public Optional<T> findById(int id) {
        return entities.stream().filter(entity -> entity.getId() == id).findFirst();
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(entities);
    }

    @Override
    public int nextId() {
        return entities.stream().mapToInt(Identifiable::getId).max().orElse(0) + 1;
    }
}
