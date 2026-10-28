package persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Identifiable;

/**
 * Implementa el almacenamiento de entidades en archivos de texto y conserva el contrato de
 * Repository.
 * @param <T> tipo de entidad identificable que se almacena en el archivo.
 */
public class TextFileRepository<T extends Identifiable> implements Repository<T> {

    private final Path file;
    private final EntityFileMapper<T> mapper;
    private final List<T> entities = new ArrayList<>();

    /**
     * Crea un repositorio asociado a un archivo y carga sus registros al inicializarse.
     * @param file ruta del archivo que almacena los registros.
     * @param mapper convertidor utilizado para leer y escribir entidades.
     * @throws UncheckedIOException si no se puede leer el archivo.
     * @throws IllegalStateException si un registro del archivo no se puede interpretar.
     */
    public TextFileRepository(Path file, EntityFileMapper<T> mapper) {
        this.file = file;
        this.mapper = mapper;
        loadFromFile();
    }

    private void loadFromFile() {
        if (!Files.exists(file)) return;
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) continue;
                try {
                    T entity = mapper.fromLine(line);
                    if (findById(entity.getId()).isPresent()) {
                        throw new IllegalArgumentException("ID duplicado: " + entity.getId());
                    }
                    entities.add(entity);
                } catch (RuntimeException error) {
                    throw new IllegalStateException("No se pudo leer " + file + ", linea " + (i + 1) + ": " + error.getMessage(), error);
                }
            }
        } catch (IOException error) {
            throw new UncheckedIOException("No se pudo leer el archivo " + file, error);
        }
    }

    /**
     * Guarda una entidad nueva y escribe el contenido actualizado en el archivo.
     * @param entity entidad que se desea almacenar o convertir.
     * @throws IllegalArgumentException si la entidad es nula o su identificador ya existe.
     * @throws UncheckedIOException si no se puede escribir el archivo.
     */
    @Override
    public void save(T entity) {
        if (entity == null) throw new IllegalArgumentException("No se puede guardar un objeto nulo.");
        if (findById(entity.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un registro con ID " + entity.getId() + ".");
        }
        entities.add(entity);
        try {
            writeToFile();
        } catch (RuntimeException error) {
            entities.remove(entities.size() - 1);
            throw error;
        }
    }

    /**
     * Actualiza una entidad existente y persiste el contenido actualizado.
     * @param entity entidad que se desea almacenar o convertir.
     * @throws IllegalArgumentException si la entidad es nula o no existe un registro con ese identificador.
     * @throws UncheckedIOException si no se puede escribir el archivo.
     */
    @Override
    public void update(T entity) {
        if (entity == null) throw new IllegalArgumentException("No se puede actualizar un objeto nulo.");
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId() == entity.getId()) {
                T previous = entities.set(i, entity);
                try {
                    writeToFile();
                } catch (RuntimeException error) {
                    entities.set(i, previous);
                    throw error;
                }
                return;
            }
        }
        throw new IllegalArgumentException("No existe un registro con ID " + entity.getId() + " para actualizar.");
    }

    private void writeToFile() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            List<String> lines = entities.stream().map(mapper::toLine).toList();
            Files.write(file, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException error) {
            throw new UncheckedIOException("No se pudo guardar el archivo " + file, error);
        }
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
     * Devuelve una copia no modificable de las entidades cargadas.
     * @return lista de las entidades almacenadas.
     */
    @Override
    public List<T> findAll() {
        return List.copyOf(entities);
    }

    /**
     * Calcula el siguiente identificador disponible a partir de los registros cargados.
     * @return siguiente identificador disponible.
     */
    @Override
    public int nextId() {
        return entities.stream().mapToInt(Identifiable::getId).max().orElse(0) + 1;
    }
}
