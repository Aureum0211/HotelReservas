package model;

import validation.Validator;

public abstract class Person implements Identifiable {

    private final int id;
    private final String name;
    private final String documentNumber;
    private String email;
    private String phone;
    private String city;
    private boolean active = true;

    protected Person(int id, String name, String documentNumber, String email, String phone, String city) {
        this.id = Validator.requirePositive(id, "El id de la persona");
        this.name = Validator.requireText(name, "El nombre", 60);
        this.documentNumber = Validator.requireText(documentNumber, "El documento", 20);
        this.email = Validator.requireEmail(email);
        this.phone = Validator.requirePhone(phone);
        this.city = Validator.requireText(city, "La ciudad", 50);
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

        this.email = Validator.requireEmail(email);

    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) {

        this.phone = Validator.requirePhone(phone);

    }

    public String getCity() { return city; }

    public void setCity(String city) {

        this.city = Validator.requireText(city, "La ciudad", 50);

    }

    public boolean isActive() { return active; }

    @Override
    public String toString() { return name + " (" + documentNumber + ")"; }
}
