package exceptions;

/** Excepcion para operaciones que incumplen una regla del sistema. */
public class BusinessRuleException extends HotelException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
