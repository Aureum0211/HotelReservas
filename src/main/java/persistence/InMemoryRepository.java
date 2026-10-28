package persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Identifiable;

/**
 * Implementa el contrato de almacenamiento usando una lista en memoria, sin conservar
 * datos entre ejecuciones.
 * @param <T> tipo de entidad identificable que se conserva en memoria.
 */
public class InMemoryRepository<T extends Identifiable> implements Repository<T> {

    private final List<T> entities = new ArrayList<>();

    /**
     * Agrega una entidad nueva y rechaza valores nulos o identificadores repetidos.
     * @param entity entidad que se desea almacenar o convertir.
     * @throws IllegalArgumentException si la entidad es nula o su identificador ya existe.
     */
    @Override
    public void save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("No se puede guardar un objeto nulo.");
        }
        if (findById(entity.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un registro con ID " + entity.getId() + ".");
        }
        entities.add(entity);
    }

    /**
     * Reemplaza una entidad existente con el mismo identificador.
     * @param entity entidad que se desea almacenar o convertir.
     * @throws IllegalArgumentException si la entidad es nula o no existe un registro con ese identificador.
     */
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

    /**
     * Busca una entidad por su identificador.
     * @param id identificador numerico del registro.
     * @return resultado opcional que contiene la entidad si existe.
     */
    @Override
    public Optional<T> findById(int id) {
        return entities.stream().filter(entity -> entity.getId() == id).findFirst();
    }

    /**
     * Devuelve una lista no modificable con las entidades almacenadas.
     * @return lista de las entidades almacenadas.
     */
    @Override
    public List<T> findAll() {
        return List.copyOf(entities);
    }

    /**
     * Calcula el siguiente identificador disponible a partir del mayor identificador
     * guardado.
     * @return siguiente identificador disponible.
     */
    @Override
    public int nextId() {
        return entities.stream().mapToInt(Identifiable::getId).max().orElse(0) + 1;
    }
}
