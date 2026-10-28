package persistence;

import model.Identifiable;

/**
 * Define las operaciones para convertir una entidad a una linea de texto y reconstruirla
 * desde esa linea.
 * @param <T> tipo de entidad que se convierte a texto y desde texto.
 */
public interface EntityFileMapper<T extends Identifiable> {

    /**
     * Convierte una entidad en su representacion de texto para guardarla.
     * @param entity entidad que se desea almacenar o convertir.
     * @return representacion textual de la entidad.
     */
    String toLine(T entity);

    /**
     * Reconstruye una entidad a partir de una linea de texto.
     * @param line linea de texto que se va a interpretar.
     * @return entidad reconstruida a partir de la linea.
     */
    T fromLine(String line);
}
