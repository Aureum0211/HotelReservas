package exceptions;

/**
 * Indica que no se encontro un registro solicitado en el almacenamiento del sistema.
 */
public class ResourceNotFoundException extends HotelException {

    /**
     * Construye una excepcion para indicar que no se encontro el recurso solicitado.
     * @param message mensaje descriptivo de la excepcion.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
