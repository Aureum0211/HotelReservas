package model;

import validation.Validator;

/** Datos de un trabajador del hotel. */
public class Employee extends Person {

    /** Roles disponibles para los trabajadores. */
    public enum Role {
        
        RECEPTIONIST("Recepcionista"),
        
        ADMIN("Administrador");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        public String getLabel() { return label; }

        public boolean isAtLeast(Role minimum) { return this == ADMIN || this == minimum; }
    }

    private final Role role;
    private final String username;
    private String password;

    public Employee(int id, String name, String documentNumber, String email, String phone,
                    String city, Role role, String username, String password) {
        super(id, name, documentNumber, email, phone, city);
        this.role = Validator.requireNonNull(role, "El rol");
        this.username = Validator.requireText(username, "El usuario", 20);
        this.password = Validator.requireText(password, "La contrasena", 30);
    }

    public Role getRole() { return role; }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public void changePassword(String password) {
        this.password = Validator.requireText(password, "La contrasena", 30);
    }

    @Override
    public String describe() {
        return "Trabajador: " + getName() + " | Usuario: " + username + " | Grado: " + role.getLabel();
    }
}
