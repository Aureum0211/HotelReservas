package validation;

import exceptions.InvalidDataException;

public final class Validator {

    private Validator() { }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new InvalidDataException(field + " es obligatorio.");
        }
        return value;
    }

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

    public static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new InvalidDataException(field + " debe ser mayor que cero.");
        }
        return value;
    }

    public static double requirePositive(double value, String field) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new InvalidDataException(field + " debe ser un numero mayor que cero.");
        }
        return value;
    }

    public static double requirePercentage(double value, String field) {
        if (!Double.isFinite(value) || value <= 0 || value >= 100) {
            throw new InvalidDataException(field + " debe ser mayor que 0 y menor que 100.");
        }
        return value;
    }
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
