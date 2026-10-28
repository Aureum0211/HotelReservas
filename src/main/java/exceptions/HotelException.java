package exceptions;

/**
 * Clase base para las excepciones propias que puede producir la aplicacion del hotel.
 */
public abstract class HotelException extends RuntimeException {

    /**
     * Construye la excepcion base del sistema con un mensaje descriptivo.
     * @param message mensaje descriptivo de la excepcion.
     */
    protected HotelException(String message) {
        super(message);
    }
}
