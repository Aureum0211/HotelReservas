package model;

/**
 * Persona del hotel. Es abstracta porque toda persona es un Client o un Employee.
 */
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
            throw new IllegalArgumentException("El id de la persona debe ser mayor que cero.");
        }
        this.id = id;
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.name = name;
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        this.documentNumber = documentNumber;
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        this.email = email;
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener 10 dígitos.");
        }
        this.phone = phone;
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("La ciudad es obligatorio.");
        }
        this.city = city;
    }

    /** cada subclase agrega sus propios datos. */
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

            throw new IllegalArgumentException("El correo no tiene un formato válido.");

        }

        this.email = email;

    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {

        if (phone == null || !phone.matches("\\d{10}")) {

            throw new IllegalArgumentException("El teléfono debe tener 10 dígitos.");

        }

        this.phone = phone;

    }

    public String getCity() { return city; }

    public void setCity(String city) {

        if (city == null || city.isBlank()) {

            throw new IllegalArgumentException("La ciudad es obligatorio.");

        }

        this.city = city;

    }

    public boolean isActive() { return active; }

    @Override
    public String toString() { return name + " (" + documentNumber + ")"; }
}
