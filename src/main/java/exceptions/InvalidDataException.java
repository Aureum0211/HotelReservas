package exceptions;

/** Excepcion para datos no validos. */
public class InvalidDataException extends HotelException {

    public InvalidDataException(String message) {
        super(message);
    }
}
