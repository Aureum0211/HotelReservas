package model;

import exceptions.InvalidDataException;

public class Employee extends Person {

    public enum Role { ADMIN, RECEPTIONIST }

    private Role role;
    private final String username;
    private String password;

    public Employee(int id, String name, String documentNumber, String email, String phone,
                    String city, Role role, String username, String password) {
        super(id, name, documentNumber, email, phone, city);
        if (role == null) {
            throw new InvalidDataException("El rol es obligatorio.");
        }
        this.role = role;
        if (username == null || username.isBlank()) {
            throw new InvalidDataException("El usuario es obligatorio.");
        }
        this.username = username;
        if (password == null || password.isBlank()) {
            throw new InvalidDataException("La contrasena es obligatorio.");
        }
        this.password = password;
    }

    public Role getRole() { return role; }

    public void changeRole(Role role) {

        if (role == null) {

            throw new InvalidDataException("El rol es obligatorio.");

        }

        this.role = role;

    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public void changePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidDataException("La contrasena es obligatorio.");
        }
        this.password = password;
    }

    @Override
    public String describe() {
        return "Trabajador: " + getName() + " | Usuario: " + username + " | Rol: " + role;
    }
}
