package persistence;

import java.util.List;
import java.util.Optional;
import model.Identifiable;

/**
 * Define las operaciones comunes para guardar, actualizar, consultar y asignar
 * identificadores a las entidades.
 * @param <T> tipo de entidad identificable que administra el repositorio.
 */
public interface Repository<T extends Identifiable> {

    /**
     * Guarda una entidad nueva en el almacenamiento.
     * @param entity entidad que se desea almacenar o convertir.
     */
    void save(T entity);

    /**
     * Actualiza una entidad que ya existe en el almacenamiento.
     * @param entity entidad que se desea almacenar o convertir.
     */
    void update(T entity);

    /**
     * Busca una entidad por su identificador.
     * @param id identificador numerico del registro.
     * @return resultado opcional que contiene la entidad si existe.
     */
    Optional<T> findById(int id);

    /**
     * Devuelve todas las entidades almacenadas.
     * @return lista de las entidades almacenadas.
     */
    List<T> findAll();

    /**
     * Devuelve el siguiente identificador que puede asignarse.
     * @return siguiente identificador disponible.
     */
    int nextId();
}
