package model;

import validation.Validator;

/**
 * Representa a un trabajador del hotel, con rol, usuario y contrasena de acceso para la
 * demostracion.
 */
public class Employee extends Person {

    /**
     * Grados que puede tener un trabajador del hotel. Cada grado incluye todo lo que puede
     * hacer el grado anterior.
     */
    public enum Role {
        /** Atiende clientes y gestiona reservas. */
        RECEPTIONIST("Recepcionista"),
        /** Puede realizar las operaciones administrativas del hotel. */
        ADMIN("Administrador");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        /**
         * Devuelve el nombre del grado para mostrarlo en pantalla.
         * @return nombre del grado en espanol.
         */
        public String getLabel() { return label; }

        /**
         * Indica si este grado es igual o superior al minimo indicado.
         * @param minimum grado minimo requerido.
         * @return true si este grado alcanza el minimo.
         */
        public boolean isAtLeast(Role minimum) { return this == ADMIN || this == minimum; }
    }

    private final Role role;
    private final String username;
    private String password;

    /**
     * Crea un trabajador con sus datos personales, rol y credenciales de acceso.
     * @param id identificador numerico del registro.
     * @param name nombre de la persona.
     * @param documentNumber numero de documento.
     * @param email correo electronico.
     * @param phone numero de telefono.
     * @param city ciudad asociada a la persona.
     * @param role rol asignado al trabajador.
     * @param username nombre de usuario.
     * @param password contrasena del trabajador.
     */
    public Employee(int id, String name, String documentNumber, String email, String phone,
                    String city, Role role, String username, String password) {
        super(id, name, documentNumber, email, phone, city);
        this.role = Validator.requireNonNull(role, "El rol");
        this.username = Validator.requireText(username, "El usuario", 20);
        this.password = Validator.requireText(password, "La contrasena", 30);
    }

    /**
     * Devuelve el rol asignado al trabajador.
     * @return rol actual del trabajador.
     */
    public Role getRole() { return role; }

    /**
     * Devuelve el nombre de usuario del trabajador.
     * @return nombre de usuario del trabajador.
     */
    public String getUsername() { return username; }

    /**
     * Devuelve la contrasena almacenada para esta demostracion academica.
     * @return contrasena almacenada para la demostracion.
     */
    public String getPassword() { return password; }

    /**
     * Actualiza la contrasena del trabajador despues de validarla.
     * @param password contrasena del trabajador.
     */
    public void changePassword(String password) {
        this.password = Validator.requireText(password, "La contrasena", 30);
    }

    /**
     * Devuelve una descripcion textual del trabajador.
     * @return descripcion textual de la persona o entidad.
     */
    @Override
    public String describe() {
        return "Trabajador: " + getName() + " | Usuario: " + username + " | Grado: " + role.getLabel();
    }
}
