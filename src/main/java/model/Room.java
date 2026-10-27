package model;

import validation.Validator;

public class Room implements Identifiable {

    public enum Type { SINGLE, DOUBLE, SUITE }

    private final int id;
    private final String number;
    private final Type type;
    private final int capacity;
    private double pricePerNight;
    private boolean available;

    public Room(int id, String number, Type type, int capacity, double pricePerNight, boolean available) {
        this.id = Validator.requirePositive(id, "El id de la habitacion");
        this.number = Validator.requireText(number, "El numero de habitacion", 10);
        this.type = Validator.requireNonNull(type, "El tipo de habitacion");
        this.capacity = Validator.requirePositive(capacity, "La capacidad");
        this.pricePerNight = Validator.requirePositive(pricePerNight, "El precio");
        this.available = available;
    }

    public void markAvailable() { this.available = true; }

    public void markUnavailable() { this.available = false; }

    @Override
    public int getId() { return id; }

    public String getNumber() { return number; }

    public Type getType() { return type; }

    public int getCapacity() { return capacity; }

    public double getPricePerNight() { return pricePerNight; }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = Validator.requirePositive(pricePerNight, "El precio");
    }

    public boolean isAvailable() { return available; }

    @Override
    public String toString() { return "Habitacion " + number + " (" + type + ")"; }
}
