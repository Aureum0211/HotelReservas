package model;

/** Persona que hace reservas. Su categoría premia la frecuencia de uso. */
public class Client extends Person {

    public enum Category { NEW, FREQUENT, VIP }

    private Category category;

    public Client(int id, String name, String documentNumber, String email, String phone,
                  String city, Category category) {
        super(id, name, documentNumber, email, phone, city);
        if (category == null) {
            throw new IllegalArgumentException("La categoría es obligatorio.");
        }
        this.category = category;
    }

    public Category getCategory() { return category; }

    public void changeCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("La categoría es obligatorio.");
        }
        this.category = category;
    }

    @Override
    public String describe() {
        return "Cliente: " + getName() + " | Doc: " + getDocumentNumber() + " | " + getEmail()
                + " | Categoría: " + category;
    }
}
