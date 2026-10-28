package exceptions;

/**
 * Representa un intento de ejecutar una operacion que incumple una regla del negocio del
 * hotel.
 */
public class BusinessRuleException extends HotelException {

    /**
     * Construye una excepcion para indicar que una operacion incumple una regla del
     * negocio.
     * @param message mensaje descriptivo de la excepcion.
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
