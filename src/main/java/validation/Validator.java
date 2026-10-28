package validation;

import exceptions.InvalidDataException;

/**
 * Centraliza las validaciones reutilizables de datos para evitar repetirlas en las clases
 * del dominio.
 */
public final class Validator {

    private Validator() { }

    /**
     * Comprueba que un valor obligatorio no sea nulo y devuelve el mismo valor.
     * @param <T> tipo del valor que se valida.
     * @param value valor que se desea validar.
     * @param field nombre del dato que se esta validando.
     * @return el mismo valor recibido, si no es nulo.
     * @throws InvalidDataException si el valor es nulo.
     */
    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new InvalidDataException(field + " es obligatorio.");
        }
        return value;
    }

    /**
     * Comprueba que un texto no sea nulo ni vacio, elimina espacios externos y limita su
     * longitud.
     * @param value valor que se desea validar.
     * @param field nombre del dato que se esta validando.
     * @param maxLength longitud maxima permitida para el texto.
     * @return texto validado y sin espacios externos.
     * @throws InvalidDataException si el texto es nulo, vacio o supera la longitud maxima.
     */
    public static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidDataException(field + " es obligatorio.");
        }
        String text = value.trim();
        if (text.length() > maxLength) {
            throw new InvalidDataException(field + " no puede superar " + maxLength + " caracteres.");
        }
        return text;
    }

    /**
     * Comprueba que un numero sea finito cuando corresponda y mayor que cero.
     * @param value valor que se desea validar.
     * @param field nombre del dato que se esta validando.
     * @return el numero validado.
     * @throws InvalidDataException si el numero no es finito o no es mayor que cero.
     */
    public static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new InvalidDataException(field + " debe ser mayor que cero.");
        }
        return value;
    }

    /**
     * Comprueba que un numero sea finito cuando corresponda y mayor que cero.
     * @param value valor que se desea validar.
     * @param field nombre del dato que se esta validando.
     * @return el numero validado.
     * @throws InvalidDataException si el numero no es finito o no es mayor que cero.
     */
    public static double requirePositive(double value, String field) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new InvalidDataException(field + " debe ser un numero mayor que cero.");
        }
        return value;
    }

    /**
     * Comprueba que el porcentaje sea mayor que cero y menor que cien.
     * @param value valor que se desea validar.
     * @param field nombre del dato que se esta validando.
     * @return el porcentaje validado.
     * @throws InvalidDataException si el porcentaje no es mayor que cero y menor que cien.
     */
    public static double requirePercentage(double value, String field) {
        if (!Double.isFinite(value) || value <= 0 || value >= 100) {
            throw new InvalidDataException(field + " debe ser mayor que 0 y menor que 100.");
        }
        return value;
    }

    /**
     * Valida el formato basico del correo y devuelve su version en minusculas.
     * @param value valor que se desea validar.
     * @return correo validado y convertido a minusculas.
     * @throws InvalidDataException si el correo no cumple el formato esperado.
     */
    public static String requireEmail(String value) {
        String email = requireText(value, "El correo", 50);
        int at = email.indexOf('@');
        int lastAt = email.lastIndexOf('@');
        int dot = email.lastIndexOf('.');

        if (at <= 0 || at != lastAt || dot < at + 2
                || dot == email.length() - 1 || email.contains(" ")) {
            throw new InvalidDataException("El correo no tiene un formato valido.");
        }

        return email.toLowerCase();
    }

    /**
     * Valida que el telefono tenga exactamente diez digitos numericos.
     * @param value valor que se desea validar.
     * @return telefono validado.
     * @throws InvalidDataException si el telefono no tiene exactamente diez digitos.
     */
    public static String requirePhone(String value) {
        String phone = requireText(value, "El telefono", 10);

        if (phone.length() != 10) {
            throw new InvalidDataException("El telefono debe tener exactamente 10 digitos.");
        }

        for (int i = 0; i < phone.length(); i++) {
            char digit = phone.charAt(i);
            if (digit < '0' || digit > '9') {
                throw new InvalidDataException("El telefono solo puede contener digitos.");
            }
        }

        return phone;
    }
}
