package model;

import validation.Validator;

/**
 * Representa a un cliente del hotel. Hereda los datos comunes de Persona y mantiene su
 * categoria.
 */
public class Client extends Person {

    /**
     * Categorias disponibles para identificar la frecuencia de uso del cliente.
     */
    public enum Category {
        /** Cliente que comienza a utilizar los servicios del hotel. */
        NEW,
        /** Cliente que utiliza los servicios del hotel de forma recurrente. */
        FREQUENT,
        /** Cliente clasificado en la categoria VIP. */
        VIP
    }

    private Category category;

    /**
     * Crea un cliente con sus datos personales y categoria.
     * @param id identificador numerico del registro.
     * @param name nombre de la persona.
     * @param documentNumber numero de documento.
     * @param email correo electronico.
     * @param phone numero de telefono.
     * @param city ciudad asociada a la persona.
     * @param category categoria asignada al cliente.
     */
    public Client(int id, String name, String documentNumber, String email, String phone,
                  String city, Category category) {
        super(id, name, documentNumber, email, phone, city);
        this.category = Validator.requireNonNull(category, "La categoria");
    }

    /**
     * Devuelve la categoria actual del cliente.
     * @return categoria actual del cliente.
     */
    public Category getCategory() { return category; }

    /**
     * Cambia la categoria del cliente despues de validar que el valor no sea nulo.
     * @param category categoria asignada al cliente.
     */
    public void changeCategory(Category category) {
        this.category = Validator.requireNonNull(category, "La categoria");
    }

    /**
     * Devuelve una descripcion textual del cliente.
     * @return descripcion textual de la persona o entidad.
     */
    @Override
    public String describe() {
        return "Cliente: " + getName() + " | Doc: " + getDocumentNumber() + " | " + getEmail()
                + " | Categoria: " + category;
    }
}
