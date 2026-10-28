package exceptions;

/**
 * Indica que un dato recibido no cumple las restricciones de validacion requeridas.
 */
public class InvalidDataException extends HotelException {

    /**
     * Construye una excepcion para indicar que un dato no supera las validaciones
     * requeridas.
     * @param message mensaje descriptivo de la excepcion.
     */
    public InvalidDataException(String message) {
        super(message);
    }
}
