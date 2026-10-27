package model;

import validation.Validator;

public class Employee extends Person {

    public enum Role { ADMIN, RECEPTIONIST }

    private Role role;
    private final String username;
    private String password;

    public Employee(int id, String name, String documentNumber, String email, String phone,
                    String city, Role role, String username, String password) {
        super(id, name, documentNumber, email, phone, city);
        this.role = Validator.requireNonNull(role, "El rol");
        this.username = Validator.requireText(username, "El usuario", 30);
        this.password = Validator.requireText(password, "La contrasena", 50);
    }

    public Role getRole() { return role; }

    public void changeRole(Role role) {

        this.role = Validator.requireNonNull(role, "El rol");

    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public void changePassword(String password) {
        this.password = Validator.requireText(password, "La contrasena", 50);
    }

    @Override
    public String describe() {
        return "Trabajador: " + getName() + " | Usuario: " + username + " | Rol: " + role;
    }
}
