package model;

import exceptions.InvalidDataException;

public abstract class Person implements Identifiable {

    private final int id;
    private final String name;
    private final String documentNumber;
    private String email;
    private String phone;
    private String city;
    private boolean active = true;

    protected Person(int id, String name, String documentNumber, String email, String phone, String city) {
        if (id <= 0) {
            throw new InvalidDataException("El id de la persona debe ser mayor que cero.");
        }
        this.id = id;
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("El nombre es obligatorio.");
        }
        this.name = name;
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new InvalidDataException("El documento es obligatorio.");
        }
        this.documentNumber = documentNumber;
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new InvalidDataException("El correo no tiene un formato valido.");
        }
        this.email = email;
        this.phone = validatePhone(phone);
        if (city == null || city.isBlank()) {
            throw new InvalidDataException("La ciudad es obligatorio.");
        }
        this.city = city;
    }

    public abstract String describe();

    public void activate() { this.active = true; }

    public void deactivate() { this.active = false; }

    @Override
    public int getId() { return id; }

    public String getName() { return name; }

    public String getDocumentNumber() { return documentNumber; }

    public String getEmail() { return email; }

    public void setEmail(String email) {

        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {

            throw new InvalidDataException("El correo no tiene un formato valido.");

        }

        this.email = email;

    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {
        this.phone = validatePhone(phone);
    }

    public String getCity() { return city; }

    public void setCity(String city) {

        if (city == null || city.isBlank()) {

            throw new InvalidDataException("La ciudad es obligatorio.");

        }

        this.city = city;

    }

    private static String validatePhone(String phone) {
        if (phone == null || phone.length() != 10) {
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

    public boolean isActive() { return active; }

    @Override
    public String toString() { return name + " (" + documentNumber + ")"; }
}
