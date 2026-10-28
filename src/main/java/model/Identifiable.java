package model;

/**
 * Define el contrato minimo para las entidades que cuentan con un identificador numerico.
 */
public interface Identifiable {

    /**
     * Devuelve el id de la entidad.
     * @return identificador numerico de la entidad.
     */
    int getId();
}
