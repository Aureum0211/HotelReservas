package model;

import validation.Validator;

public class Client extends Person {

    public enum Category { NEW, FREQUENT, VIP }

    private Category category;

    public Client(int id, String name, String documentNumber, String email, String phone,
                  String city, Category category) {
        super(id, name, documentNumber, email, phone, city);
        this.category = Validator.requireNonNull(category, "La categoria");
    }

    public Category getCategory() { return category; }

    public void changeCategory(Category category) {
        this.category = Validator.requireNonNull(category, "La categoria");
    }

    @Override
    public String describe() {
        return "Cliente: " + getName() + " | Doc: " + getDocumentNumber() + " | " + getEmail()
                + " | Categoria: " + category;
    }
}
