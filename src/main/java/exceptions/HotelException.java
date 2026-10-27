package exceptions;

public abstract class HotelException extends RuntimeException {

    protected HotelException(String message) {
        super(message);
    }
}
