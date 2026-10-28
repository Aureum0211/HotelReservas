package model;

import validation.Validator;

/**
 * Representa a un trabajador del hotel, con rol, usuario y contrasena de acceso para la
 * demostracion.
 */
public class Employee extends Person {

    /**
     * Roles que puede tener un trabajador del hotel.
     */
    public enum Role {
        /** Trabajador con permisos administrativos. */
        ADMIN,
        /** Trabajador encargado de la atencion y recepcion. */
        RECEPTIONIST
    }

    private Role role;
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
        this.username = Validator.requireText(username, "El usuario", 30);
        this.password = Validator.requireText(password, "La contrasena", 50);
    }

    /**
     * Devuelve el rol asignado al trabajador.
     * @return rol actual del trabajador.
     */
    public Role getRole() { return role; }

    /**
     * Cambia el rol del trabajador despues de validar el nuevo valor.
     * @param role rol asignado al trabajador.
     */
    public void changeRole(Role role) {

        this.role = Validator.requireNonNull(role, "El rol");

    }

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
        this.password = Validator.requireText(password, "La contrasena", 50);
    }

    /**
     * Devuelve una descripcion textual del trabajador.
     * @return descripcion textual de la persona o entidad.
     */
    @Override
    public String describe() {
        return "Trabajador: " + getName() + " | Usuario: " + username + " | Rol: " + role;
    }
}
