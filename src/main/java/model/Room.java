package model;

import validation.Validator;

/**
 * Representa una habitacion del hotel, con tipo, capacidad, precio por noche y
 * disponibilidad.
 */
public class Room implements Identifiable {

    /**
     * Tipos de habitacion disponibles en el hotel.
     */
    public enum Type {
        /** Habitacion preparada para una persona. */
        SINGLE,
        /** Habitacion preparada para dos personas. */
        DOUBLE,
        /** Habitacion tipo suite. */
        SUITE
    }

    private final int id;
    private final String number;
    private final Type type;
    private final int capacity;
    private double pricePerNight;
    private boolean available;

    /**
     * Crea una habitacion y valida sus datos principales.
     * @param id identificador numerico del registro.
     * @param number numero visible de la habitacion.
     * @param type tipo de habitacion.
     * @param capacity capacidad maxima de personas.
     * @param pricePerNight precio establecido por cada noche.
     * @param available indica si la habitacion esta disponible.
     */
    public Room(int id, String number, Type type, int capacity, double pricePerNight, boolean available) {
        this.id = Validator.requirePositive(id, "El id de la habitacion");
        this.number = Validator.requireText(number, "El numero de habitacion", 10);
        this.type = Validator.requireNonNull(type, "El tipo de habitacion");
        this.capacity = Validator.requirePositive(capacity, "La capacidad");
        this.pricePerNight = Validator.requirePositive(pricePerNight, "El precio");
        this.available = available;
    }

    /**
     * Marca la habitacion como disponible para nuevas reservas.
     */
    public void markAvailable() { this.available = true; }

    /**
     * Marca la habitacion como no disponible.
     */
    public void markUnavailable() { this.available = false; }

    /**
     * Devuelve el identificador de la habitacion.
     * @return identificador numerico de la entidad.
     */
    @Override
    public int getId() { return id; }

    /**
     * Devuelve el numero visible de la habitacion.
     * @return numero de la habitacion.
     */
    public String getNumber() { return number; }

    /**
     * Devuelve el tipo de habitacion.
     * @return tipo de habitacion.
     */
    public Type getType() { return type; }

    /**
     * Devuelve la capacidad maxima de la habitacion.
     * @return capacidad maxima de la habitacion.
     */
    public int getCapacity() { return capacity; }

    /**
     * Devuelve la tarifa por noche.
     * @return tarifa por noche aplicada.
     */
    public double getPricePerNight() { return pricePerNight; }

    /**
     * Valida y actualiza la tarifa por noche.
     * @param pricePerNight precio establecido por cada noche.
     */
    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = Validator.requirePositive(pricePerNight, "El precio");
    }

    /**
     * Indica si la habitacion esta marcada como disponible.
     * @return true si la habitacion esta disponible.
     */
    public boolean isAvailable() { return available; }

    /**
     * Devuelve una representacion breve de la habitacion.
     * @return representacion textual breve del objeto.
     */
    @Override
    public String toString() { return "Habitacion " + number + " (" + type + ")"; }
}
