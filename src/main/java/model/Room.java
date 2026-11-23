package model;

import validation.Validator;

/** Datos de una habitacion del hotel. */
public class Room implements Identifiable {

    /** Tipos de habitacion disponibles. */
    public enum Type {
        
        SINGLE,
        
        DOUBLE,
        
        SUITE
    }

    private final int id;
    private final String number;
    private final Type type;
    private final int capacity;
    private final double pricePerNight;
    private final boolean available;
    private final boolean underMaintenance;

    public Room(int id, String number, Type type, int capacity, double pricePerNight, boolean available) {
        this(id, number, type, capacity, pricePerNight, available, false);
    }

    public Room(int id, String number, Type type, int capacity, double pricePerNight,
                boolean available, boolean underMaintenance) {
        this.id = Validator.requirePositive(id, "El id de la habitacion");
        this.number = Validator.requireText(number, "El numero de habitacion", 6);
        this.type = Validator.requireNonNull(type, "El tipo de habitacion");
        this.capacity = Validator.requirePositive(capacity, "La capacidad");
        this.pricePerNight = Validator.requirePositive(pricePerNight, "El precio");
        this.available = available;
        this.underMaintenance = underMaintenance;
    }

    public boolean isUnderMaintenance() { return underMaintenance; }

    @Override
    public int getId() { return id; }

    public String getNumber() { return number; }

    public Type getType() { return type; }

    public int getCapacity() { return capacity; }

    public double getPricePerNight() { return pricePerNight; }

    public boolean isAvailable() { return available; }

    @Override
    public String toString() { return "Habitacion " + number + " (" + type + ")"; }
}
