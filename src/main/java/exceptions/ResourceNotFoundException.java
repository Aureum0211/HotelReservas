package exceptions;

/** Excepcion para registros que no existen. */
public class ResourceNotFoundException extends HotelException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
