package exceptions;

/** Excepcion base de la aplicacion. */
public abstract class HotelException extends RuntimeException {

    protected HotelException(String message) {
        super(message);
    }
}
