package model;

import validation.Validator;

/**
 * Define los datos y comportamientos comunes a los clientes y trabajadores del hotel.
 */
public abstract class Person implements Identifiable {

    private final int id;
    private final String name;
    private final String documentNumber;
    private String email;
    private String phone;
    private String city;
    private boolean active = true;

    /**
     * Inicializa los datos comunes de una persona y valida sus valores.
     * @param id identificador numerico del registro.
     * @param name nombre de la persona.
     * @param documentNumber numero de documento.
     * @param email correo electronico.
     * @param phone numero de telefono.
     * @param city ciudad asociada a la persona.
     */
    protected Person(int id, String name, String documentNumber, String email, String phone, String city) {
        this.id = Validator.requirePositive(id, "El id de la persona");
        this.name = Validator.requireText(name, "El nombre", 60);
        this.documentNumber = Validator.requireText(documentNumber, "El documento", 20);
        this.email = Validator.requireEmail(email);
        this.phone = Validator.requirePhone(phone);
        this.city = Validator.requireText(city, "La ciudad", 50);
    }

    /**
     * Devuelve la descripcion especifica de la persona; cada subclase define su propio
     * formato.
     * @return descripcion textual de la persona o entidad.
     */
    public abstract String describe();

    /**
     * Marca la persona como activa.
     */
    public void activate() { this.active = true; }

    /**
     * Marca la persona como inactiva.
     */
    public void deactivate() { this.active = false; }

    /**
     * Devuelve el identificador de la persona.
     * @return identificador numerico de la entidad.
     */
    @Override
    public int getId() { return id; }

    /**
     * Devuelve el nombre de la persona.
     * @return nombre de la persona.
     */
    public String getName() { return name; }

    /**
     * Devuelve el numero de documento de la persona.
     * @return numero de documento.
     */
    public String getDocumentNumber() { return documentNumber; }

    /**
     * Devuelve el correo electronico de la persona.
     * @return correo electronico.
     */
    public String getEmail() { return email; }

    /**
     * Valida y actualiza el correo electronico de la persona.
     * @param email correo electronico.
     */
    public void setEmail(String email) {

        this.email = Validator.requireEmail(email);

    }

    /**
     * Devuelve el telefono de la persona.
     * @return numero de telefono.
     */
    public String getPhone() { return phone; }

    /**
     * Valida y actualiza el telefono de la persona.
     * @param phone numero de telefono.
     */
    public void setPhone(String phone) {

        this.phone = Validator.requirePhone(phone);

    }

    /**
     * Devuelve la ciudad asociada a la persona.
     * @return ciudad asociada a la persona.
     */
    public String getCity() { return city; }

    /**
     * Valida y actualiza la ciudad asociada a la persona.
     * @param city ciudad asociada a la persona.
     */
    public void setCity(String city) {

        this.city = Validator.requireText(city, "La ciudad", 50);

    }

    /**
     * Indica si la persona se encuentra activa.
     * @return true si la reserva o persona esta activa; de lo contrario, false.
     */
    public boolean isActive() { return active; }

    /**
     * Devuelve una representacion breve de la persona.
     * @return representacion textual breve del objeto.
     */
    @Override
    public String toString() { return name + " (" + documentNumber + ")"; }
}
